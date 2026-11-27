package dieterbaier.letterwright.adapter.in.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.api.BusinessReference;
import dieterbaier.letterwright.api.SavedLetter;
import dieterbaier.letterwright.api.SaveResult;
import dieterbaier.letterwright.api.LetterTemplateVersion;
import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.letter.DocumentReference;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.domain.model.letter.LetterState;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Brücke zu {@code features/letter/letter-of-die-api-speichern.feature}: ein Test je Szenario, benannt nach
 * dem Szenariotitel. Der Anwendungsfall ist eine Attrappe, die Transaktionen verwaltet die {@link FakeTransactionManager}.
 * Erwartete Werte baut der Test selbst auf, nicht mit der {@link Mapping}.
 */
class ApiSaveLetterTest {

    private static final UUID LETTER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID REMINDER = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID LETTERHEAD = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final Instant CREATED = Instant.parse("2026-11-24T10:00:00Z");

    // Grundlage: Erika hat eine Vorschau der Mahnung in Version 2 zum Kunden 4711 erzeugt.
    private final UUID previewOfErika = UUID.randomUUID();
    private final Letter fromThePreview = Letter.restore(new LetterId(LETTER),
            new SnapshotId(previewOfErika),
            new dieterbaier.letterwright.domain.model.BusinessReference("Kunde", "4711"),
            new VersionReference(new TemplateId(LETTERHEAD), VersionNumber.FIRST),
            new VersionReference(new TemplateId(REMINDER), new VersionNumber(2)), new DocumentReference("store/1"),
            Checksum.of(new byte[] {1}), CREATED, new LetterState.Visible());

    private final FakeTransactionManager transactions = new FakeTransactionManager();
    private final UseCase useCase = new UseCase(transactions);
    private final ApiSaveLetter api = new ApiSaveLetter(useCase, transactions);
    private final TransactionTemplate host = new TransactionTemplate(transactions);

    {
        useCase.result = () -> new dieterbaier.letterwright.usecases.in.SaveResult.Saved(
                fromThePreview);
    }

    private static final SavedLetter EXPECTED = new SavedLetter(LETTER,
            new BusinessReference("Kunde", "4711"), new LetterTemplateVersion(REMINDER, 2), "erika", CREATED);

    @Test
    void theHostReceivesTheIdentifiersOfTheSavedLetter() {
        var result = api.save("erika", previewOfErika);

        assertThat(result).isEqualTo(new SaveResult.Saved(EXPECTED));
        assertThat(useCase.invocations).containsExactly(new Invocation(new UserId("erika"),
                new SnapshotId(previewOfErika)));
    }

    @Test
    void aRepetitionReceivesTheAlreadySavedLetter() {
        useCase.result = () ->
                new dieterbaier.letterwright.usecases.in.SaveResult.AlreadySaved(fromThePreview);

        var result = api.save("erika", previewOfErika);

        assertThat(result).isEqualTo(new SaveResult.AlreadySaved(EXPECTED));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"SnapshotUnknown", "SnapshotExpired", "NotAllowed", "DataContextChanged",
        "NotConformant"})
    void aBusinessRejectionComesBackAsResult(String rejection) {
        useCase.result = () -> rejectionOfUseCase(rejection);

        var result = api.save("erika", previewOfErika);

        assertThat(result).isEqualTo(switch (rejection) {
            case "SnapshotUnknown" -> new SaveResult.SnapshotUnknown();
            case "SnapshotExpired" -> new SaveResult.SnapshotExpired();
            case "NotAllowed" -> new SaveResult.NotAllowed();
            case "DataContextChanged" -> new SaveResult.DataContextChanged();
            default -> new SaveResult.NotConformant(List.of("PDF/UA-1: Bild ohne Alternativtext"));
        });
    }

    @Test
    void savingJoinsTheHostsTransaction() {
        host.executeWithoutResult(status -> api.save("erika", previewOfErika));

        assertThat(transactions.ranAtInvocation).as("in der Transaktion des Hosts").isTrue();
        assertThat(transactions.openedAtInvocation).as("keine weitere Transaktion").isEqualTo(1);
        assertThat(transactions.opened).isEqualTo(1);
        assertThat(transactions.committed).isEqualTo(1);
    }

    @Test
    void withoutAHostTransactionSavingOpensItsOwn() {
        api.save("erika", previewOfErika);

        assertThat(transactions.ranAtInvocation).isTrue();
        assertThat(transactions.opened).isEqualTo(1);
        assertThat(transactions.committed).isEqualTo(1);
        assertThat(transactions.rolledBack).isZero();
    }

    @Test
    void aBusinessRejectionLetsTheHostsTransactionCommit() {
        useCase.result = () -> rejectionOfUseCase("NotAllowed");

        var result = host.execute(status -> api.save("erika", previewOfErika));

        assertThat(result).isEqualTo(new SaveResult.NotAllowed());
        assertThat(transactions.rollbackOnly).as("nicht rollback-only").isFalse();
        assertThat(transactions.committed).isEqualTo(1);
        assertThat(transactions.rolledBack).isZero();
    }

    @Test
    void aTechnicalErrorRollsBackTheHostsTransaction() {
        useCase.result = () -> {
            throw new IllegalStateException("veraPDF nicht erreichbar");
        };
        List<RuntimeException> errorAtHost = new ArrayList<>();

        // Der Host fängt den Fehler und versucht trotzdem zu committen.
        assertThatThrownBy(() -> host.executeWithoutResult(status -> {
            try {
                api.save("erika", previewOfErika);
            } catch (IllegalStateException error) {
                errorAtHost.add(error);
            }
        })).isInstanceOf(UnexpectedRollbackException.class);

        assertThat(errorAtHost).singleElement().extracting(Throwable::getMessage)
                .isEqualTo("veraPDF nicht erreichbar");
        assertThat(transactions.rollbackOnly).isTrue();
        assertThat(transactions.committed).isZero();
        assertThat(transactions.rolledBack).isEqualTo(1);
    }

    @Test
    void ifTheHostRollsBackSavingRollsBackToo() {
        var result = host.execute(status -> {
            SaveResult saved = api.save("erika", previewOfErika);
            status.setRollbackOnly();
            return saved;
        });

        assertThat(result).isInstanceOf(SaveResult.Saved.class);
        assertThat(transactions.opened).isEqualTo(1);
        assertThat(transactions.committed).isZero();
        assertThat(transactions.rolledBack).isEqualTo(1);
    }

    @Test
    void anInvalidUserIdRollsBackTheHostsTransaction() {
        List<RuntimeException> errorAtHost = new ArrayList<>();

        // Der Host fängt den Fehler und versucht trotzdem zu committen.
        assertThatThrownBy(() -> host.executeWithoutResult(status -> {
            try {
                api.save(" ", previewOfErika);
            } catch (IllegalArgumentException error) {
                errorAtHost.add(error);
            }
        })).isInstanceOf(UnexpectedRollbackException.class);

        assertThat(errorAtHost).singleElement().extracting(Throwable::getMessage).asString()
                .contains("Benutzerkennung");
        assertThat(useCase.invocations).isEmpty();
        assertThat(transactions.rollbackOnly).isTrue();
        assertThat(transactions.committed).isZero();
        assertThat(transactions.rolledBack).isEqualTo(1);
    }

    /** Kein Szenario: Die Java-API ist ohne Rückruf verdrahtet (ADR-023). Kommt die Ablehnung doch, rollt sie zurück. */
    @Test
    void aRejectionByTheCallbackIsAWiringError() {
        useCase.result = () -> new dieterbaier.letterwright.usecases.in.SaveResult
                .RejectedByHost("Die Aufgabe ist bereits erledigt");

        assertThatThrownBy(() -> host.executeWithoutResult(status -> api.save("erika", previewOfErika)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("keinen Rückruf");
        assertThat(transactions.committed).isZero();
        assertThat(transactions.rolledBack).isEqualTo(1);
    }

    private static dieterbaier.letterwright.usecases.in.SaveResult rejectionOfUseCase(
            String rejection) {
        return switch (rejection) {
            case "SnapshotUnknown" -> new dieterbaier.letterwright.usecases.in.SaveResult
                    .SnapshotUnknown();
            case "SnapshotExpired" -> new dieterbaier.letterwright.usecases.in.SaveResult
                    .SnapshotExpired();
            case "NotAllowed" -> new dieterbaier.letterwright.usecases.in.SaveResult.NotAllowed();
            case "DataContextChanged" -> new dieterbaier.letterwright.usecases.in.SaveResult
                    .DataContextChanged();
            case "NotConformant" -> new dieterbaier.letterwright.usecases.in.SaveResult.NotConformant(
                    List.of("PDF/UA-1: Bild ohne Alternativtext"));
            default -> throw new IllegalArgumentException(rejection);
        };
    }

    private record Invocation(UserId user, SnapshotId snapshot) {}

    /** Attrappe des eingehenden Ports; merkt sich die Aufrufe und ob dabei eine Transaktion lief. */
    private static final class UseCase
            implements dieterbaier.letterwright.usecases.in.SaveLetter {

        private final FakeTransactionManager transactions;
        final List<Invocation> invocations = new ArrayList<>();
        Supplier<dieterbaier.letterwright.usecases.in.SaveResult> result = () -> {
            throw new IllegalStateException("kein Ergebnis vorbereitet");
        };

        UseCase(FakeTransactionManager transactions) {
            this.transactions = transactions;
        }

        @Override
        public dieterbaier.letterwright.usecases.in.SaveResult save(
                UserId user, SnapshotId snapshot) {
            invocations.add(new Invocation(user, snapshot));
            transactions.recordInvocation();
            return result.get();
        }
    }
}
