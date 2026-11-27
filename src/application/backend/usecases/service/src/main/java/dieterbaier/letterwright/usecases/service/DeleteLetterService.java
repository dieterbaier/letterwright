package dieterbaier.letterwright.usecases.service;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.letter.DeletionDecision;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.usecases.in.DeletionResult;
import dieterbaier.letterwright.usecases.in.DeleteLetter;
import dieterbaier.letterwright.usecases.out.Permissions;
import dieterbaier.letterwright.usecases.out.DocumentStore;
import dieterbaier.letterwright.usecases.out.DeletionRules;
import dieterbaier.letterwright.usecases.out.LetterRepository;
import java.time.Clock;

/**
 * Asks the deletion policy only once permission is granted, as the SPI promises. A non-existent, a logically deleted
 * and a forbidden letter all yield the same {@code Unknown} (QS-009).
 *
 * <p>A hard deletion removes the letter first, then its PDF. If removing the PDF fails, the PDF is left without a
 * letter; the other way round, a letter would remain whose PDF is missing.
 */
public final class DeleteLetterService implements DeleteLetter {

    private final LetterRepository letters;
    private final Permissions permissions;
    private final DeletionRules deletionRules;
    private final DocumentStore documents;
    private final Clock clock;

    public DeleteLetterService(LetterRepository letters, Permissions permissions, DeletionRules deletionRules,
            DocumentStore documents, Clock clock) {
        this.letters = letters;
        this.permissions = permissions;
        this.deletionRules = deletionRules;
        this.documents = documents;
        this.clock = clock;
    }

    @Override
    public DeletionResult delete(UserId user, LetterId id) {
        return letters.load(id)
                .filter(Letter::isVisible)
                .filter(found -> permissions.may(user, LetterAction.DELETE, found.reference()))
                .map(found -> delete(user, found))
                .orElseGet(DeletionResult.Unknown::new);
    }

    private DeletionResult delete(UserId user, Letter found) {
        return switch (deletionRules.decide(user, found)) {
            case DeletionDecision.Hard hard -> {
                letters.remove(found.id());
                documents.remove(found.document());
                yield new DeletionResult.Deleted();
            }
            case DeletionDecision.Logical logical -> {
                letters.replace(found.logicallyDeleted(clock.instant()));
                yield new DeletionResult.Deleted();
            }
            case DeletionDecision.Blocked blocked -> new DeletionResult.Blocked(blocked.reason());
        };
    }
}
