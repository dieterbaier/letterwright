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
 * Bridge to {@code features/letters/delete-letter.feature}: one test per scenario, named after the
 * scenario title. The outbound ports are {@link Fakes}.
 */
class DeleteLetterServiceTest {

    private static final Instant SAVED = Instant.parse("2026-11-20T09:00:00Z");
    private static final Instant NOW = Instant.parse("2026-11-22T11:00:00Z");
    private static final UserId ERIKA = new UserId("erika");
    private static final UserId MAX = new UserId("max");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Customer", "4711");

    private final Fakes.Host host = new Fakes.Host();
    private final Fakes.DeletionPolicy policy = new Fakes.DeletionPolicy();
    private final Fakes.Documents documents = new Fakes.Documents();
    private final Fakes.LetterStore letters = new Fakes.LetterStore(new Fakes.Snapshots());
    private final SavedLetters saved = new SavedLetters(letters, documents);
    private final DeleteLetterService deletion =
            new DeleteLetterService(letters, host, policy, documents, new Fakes.StoppedClock(NOW));

    // Background: a letter for customer 4711; Erika may delete it.
    private final Letter reminder = saved.save(CUSTOMER_4711, SAVED);

    {
        host.allow(ERIKA, LetterAction.DELETE, CUSTOMER_4711);
    }

    @Test
    void hardDeletedLetterAndPdfAreRemoved() {
        policy.decision = DeletionDecision.hard();

        var result = deletion.delete(ERIKA, reminder.id());

        assertThat(result).isEqualTo(new DeletionResult.Deleted());
        assertThat(letters.withId(reminder.id())).as("letter removed").isEmpty();
        assertThat(documents.stored).as("PDF removed").doesNotContainKey(reminder.document());
        assertThat(policy.requested).containsExactly(reminder);
    }

    @Test
    void logicallyDeletedThePdfStaysTheLetterIsNoLongerVisible() {
        policy.decision = DeletionDecision.logical();

        var result = deletion.delete(ERIKA, reminder.id());

        assertThat(result).isEqualTo(new DeletionResult.Deleted());
        assertThat(letters.withId(reminder.id()).orElseThrow().state())
                .isEqualTo(new LetterState.LogicallyDeleted(NOW));
        assertThat(documents.stored).containsKey(reminder.document());
        host.allow(ERIKA, LetterAction.VIEW, CUSTOMER_4711);
        var list = (ListResult.Found) new ListLettersService(host, letters)
                .list(ERIKA, CUSTOMER_4711, new PageRequest(0, 20));
        assertThat(list.page().entries()).isEmpty();
        assertThat(list.page().total()).isZero();
    }

    @Test
    void aBlockedDeletionNamesTheReason() {
        policy.decision = DeletionDecision.blocked("Retention period until 2036");

        var result = deletion.delete(ERIKA, reminder.id());

        assertThat(result).isEqualTo(new DeletionResult.Blocked("Retention period until 2036"));
        letterUnchanged();
    }

    @Test
    void aLetterThatIsNotAllowedBehavesLikeAMissingOne() {
        var result = deletion.delete(MAX, reminder.id());

        assertThat(result).isEqualTo(new DeletionResult.Unknown());
        assertThat(policy.requested).as("deletion policy not asked").isEmpty();
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
        assertThat(letters.withId(reminder.id()).orElseThrow().state()).isEqualTo(deleted.state());
    }

    private void letterUnchanged() {
        Letter saved = letters.withId(reminder.id()).orElseThrow();
        assertThat(saved.state()).isEqualTo(new LetterState.Visible());
        assertThat(documents.stored).containsKey(reminder.document());
    }
}
