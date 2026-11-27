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
 * Bridge to {@code features/letters/save-letter-via-api.feature}: one test per scenario, named after the scenario
 * title. The use case is a fake; the {@link FakeTransactionManager} manages the transactions. The test builds expected
 * values itself, not with {@link Mapping}.
 */
class ApiSaveLetterTest {

    private static final UUID LETTER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID REMINDER = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID LETTERHEAD = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final Instant CREATED = Instant.parse("2026-11-24T10:00:00Z");

    // Background: Erika has created a preview of the payment reminder in version 2 for customer 4711.
    private final UUID previewOfErika = UUID.randomUUID();
    private final Letter fromThePreview = Letter.restore(new LetterId(LETTER),
            new SnapshotId(previewOfErika),
            new dieterbaier.letterwright.domain.model.BusinessReference("Customer", "4711"),
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
            new BusinessReference("Customer", "4711"), new LetterTemplateVersion(REMINDER, 2), "erika", CREATED);

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
            default -> new SaveResult.NotConformant(List.of("PDF/UA-1: Image without alternative text"));
        });
    }

    @Test
    void savingJoinsTheHostsTransaction() {
        host.executeWithoutResult(status -> api.save("erika", previewOfErika));

        assertThat(transactions.ranAtInvocation).as("in the host's transaction").isTrue();
        assertThat(transactions.openedAtInvocation).as("no further transaction").isEqualTo(1);
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
        assertThat(transactions.rollbackOnly).as("not rollback-only").isFalse();
        assertThat(transactions.committed).isEqualTo(1);
        assertThat(transactions.rolledBack).isZero();
    }

    @Test
    void aTechnicalErrorRollsBackTheHostsTransaction() {
        useCase.result = () -> {
            throw new IllegalStateException("veraPDF unreachable");
        };
        List<RuntimeException> errorAtHost = new ArrayList<>();

        // The host catches the error and tries to commit anyway.
        assertThatThrownBy(() -> host.executeWithoutResult(status -> {
            try {
                api.save("erika", previewOfErika);
            } catch (IllegalStateException error) {
                errorAtHost.add(error);
            }
        })).isInstanceOf(UnexpectedRollbackException.class);

        assertThat(errorAtHost).singleElement().extracting(Throwable::getMessage)
                .isEqualTo("veraPDF unreachable");
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

        // The host catches the error and tries to commit anyway.
        assertThatThrownBy(() -> host.executeWithoutResult(status -> {
            try {
                api.save(" ", previewOfErika);
            } catch (IllegalArgumentException error) {
                errorAtHost.add(error);
            }
        })).isInstanceOf(UnexpectedRollbackException.class);

        assertThat(errorAtHost).singleElement().extracting(Throwable::getMessage).asString()
                .contains("The user ID");
        assertThat(useCase.invocations).isEmpty();
        assertThat(transactions.rollbackOnly).isTrue();
        assertThat(transactions.committed).isZero();
        assertThat(transactions.rolledBack).isEqualTo(1);
    }

    /** Not a scenario: the Java API is wired without a callback (ADR-023). If the rejection occurs anyway, it rolls back. */
    @Test
    void aRejectionByTheCallbackIsAWiringError() {
        useCase.result = () -> new dieterbaier.letterwright.usecases.in.SaveResult
                .RejectedByHost("The task is already done");

        assertThatThrownBy(() -> host.executeWithoutResult(status -> api.save("erika", previewOfErika)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no callback");
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
                    List.of("PDF/UA-1: Image without alternative text"));
            default -> throw new IllegalArgumentException(rejection);
        };
    }

    private record Invocation(UserId user, SnapshotId snapshot) {}

    /** Fake of the inbound port; records the calls and whether a transaction was running at the time. */
    private static final class UseCase
            implements dieterbaier.letterwright.usecases.in.SaveLetter {

        private final FakeTransactionManager transactions;
        final List<Invocation> invocations = new ArrayList<>();
        Supplier<dieterbaier.letterwright.usecases.in.SaveResult> result = () -> {
            throw new IllegalStateException("no result prepared");
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
