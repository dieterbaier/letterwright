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
 * Fragt die Löschrichtlinie erst, wenn die Berechtigung erteilt ist, wie es das SPI zusagt. Ein nicht vorhandenes, ein
 * logisch gelöschtes und ein nicht erlaubtes Schreiben ergeben dasselbe {@code Unknown} (QS-009).
 *
 * <p>Hart gelöscht wird erst das Schreiben, dann sein PDF. Scheitert das Entfernen des PDFs, bleibt es ohne Schreiben
 * liegen; umgekehrt bliebe ein Schreiben, dessen PDF fehlt.
 */
public final class DeleteLetterService implements DeleteLetter {

    private final LetterRepository letter;
    private final Permissions permissions;
    private final DeletionRules deletionRules;
    private final DocumentStore documents;
    private final Clock clock;

    public DeleteLetterService(LetterRepository letter, Permissions permissions, DeletionRules deletionRules,
            DocumentStore documents, Clock clock) {
        this.letter = letter;
        this.permissions = permissions;
        this.deletionRules = deletionRules;
        this.documents = documents;
        this.clock = clock;
    }

    @Override
    public DeletionResult delete(UserId user, LetterId id) {
        return letter.load(id)
                .filter(Letter::isVisible)
                .filter(found -> permissions.may(user, LetterAction.DELETE, found.reference()))
                .map(found -> delete(user, found))
                .orElseGet(DeletionResult.Unknown::new);
    }

    private DeletionResult delete(UserId user, Letter found) {
        return switch (deletionRules.decide(user, found)) {
            case DeletionDecision.Hard hard -> {
                letter.remove(found.id());
                documents.remove(found.document());
                yield new DeletionResult.Deleted();
            }
            case DeletionDecision.Logical logical -> {
                letter.replace(found.logicallyDeleted(clock.instant()));
                yield new DeletionResult.Deleted();
            }
            case DeletionDecision.Blocked blocked -> new DeletionResult.Blocked(blocked.reason());
        };
    }
}
