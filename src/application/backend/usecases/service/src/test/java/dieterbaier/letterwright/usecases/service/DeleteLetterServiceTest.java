package dieterbaier.letterwright.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.PageRequest;
import dieterbaier.letterwright.domain.model.letter.DeletionDecision;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.domain.model.letter.LetterState;
import dieterbaier.letterwright.usecases.in.DeletionResult;
import dieterbaier.letterwright.usecases.in.ListResult;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Brücke zu {@code features/letter/letter-loeschen.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die ausgehenden Ports sind {@link Fakes}.
 */
class DeleteLetterServiceTest {

    private static final Instant SAVED = Instant.parse("2026-11-20T09:00:00Z");
    private static final Instant NOW = Instant.parse("2026-11-22T11:00:00Z");
    private static final UserId ERIKA = new UserId("erika");
    private static final UserId MAX = new UserId("max");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Kunde", "4711");

    private final Fakes.Host host = new Fakes.Host();
    private final Fakes.DeletionPolicy policy = new Fakes.DeletionPolicy();
    private final Fakes.Documents documents = new Fakes.Documents();
    private final Fakes.LetterStore letter = new Fakes.LetterStore(new Fakes.Snapshots());
    private final SavedLetters saved = new SavedLetters(letter, documents);
    private final DeleteLetterService deletion =
            new DeleteLetterService(letter, host, policy, documents, new Fakes.StoppedClock(NOW));

    // Grundlage: ein Schreiben zum Kunden 4711; Erika darf löschen.
    private final Letter reminder = saved.save(CUSTOMER_4711, SAVED);

    {
        host.allow(ERIKA, LetterAction.DELETE, CUSTOMER_4711);
    }

    @Test
    void hardDeletedLetterAndPdfAreRemoved() {
        policy.decision = DeletionDecision.hard();

        var result = deletion.delete(ERIKA, reminder.id());

        assertThat(result).isEqualTo(new DeletionResult.Deleted());
        assertThat(letter.withId(reminder.id())).as("Schreiben entfernt").isEmpty();
        assertThat(documents.stored).as("PDF entfernt").doesNotContainKey(reminder.document());
        assertThat(policy.requested).containsExactly(reminder);
    }

    @Test
    void logicallyDeletedThePdfStaysTheLetterIsNoLongerVisible() {
        policy.decision = DeletionDecision.logical();

        var result = deletion.delete(ERIKA, reminder.id());

        assertThat(result).isEqualTo(new DeletionResult.Deleted());
        assertThat(letter.withId(reminder.id()).orElseThrow().state())
                .isEqualTo(new LetterState.LogicallyDeleted(NOW));
        assertThat(documents.stored).containsKey(reminder.document());
        host.allow(ERIKA, LetterAction.VIEW, CUSTOMER_4711);
        var list = (ListResult.Found) new ListLettersService(host, letter)
                .list(ERIKA, CUSTOMER_4711, new PageRequest(0, 20));
        assertThat(list.page().entries()).isEmpty();
        assertThat(list.page().total()).isZero();
    }

    @Test
    void aBlockedDeletionNamesTheReason() {
        policy.decision = DeletionDecision.blocked("Aufbewahrungsfrist bis 2036");

        var result = deletion.delete(ERIKA, reminder.id());

        assertThat(result).isEqualTo(new DeletionResult.Blocked("Aufbewahrungsfrist bis 2036"));
        letterUnchanged();
    }

    @Test
    void aLetterThatIsNotAllowedBehavesLikeAMissingOne() {
        var result = deletion.delete(MAX, reminder.id());

        assertThat(result).isEqualTo(new DeletionResult.Unknown());
        assertThat(policy.requested).as("Löschrichtlinie nicht gefragt").isEmpty();
        letterUnchanged();
    }

    @Test
    void anUnknownLetterDoesNotExist() {
        var result = deletion.delete(ERIKA, LetterId.generate());

        assertThat(result).isEqualTo(new DeletionResult.Unknown());
        assertThat(host.wasAsked()).isFalse();
        assertThat(policy.requested).isEmpty();
    }

    @Test
    void aLogicallyDeletedLetterIsNotDeletedAgain() {
        Letter deleted = saved.deleteLogically(reminder, SAVED.plusSeconds(60));

        var result = deletion.delete(ERIKA, reminder.id());

        assertThat(result).isEqualTo(new DeletionResult.Unknown());
        assertThat(host.wasAsked()).isFalse();
        assertThat(policy.requested).isEmpty();
        assertThat(letter.withId(reminder.id()).orElseThrow().state()).isEqualTo(deleted.state());
    }

    private void letterUnchanged() {
        Letter saved = letter.withId(reminder.id()).orElseThrow();
        assertThat(saved.state()).isEqualTo(new LetterState.Visible());
        assertThat(documents.stored).containsKey(reminder.document());
    }
}
