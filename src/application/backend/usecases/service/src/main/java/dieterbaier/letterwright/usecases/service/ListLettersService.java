package dieterbaier.letterwright.usecases.service;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.PageRequest;
import dieterbaier.letterwright.usecases.in.ListLetters;
import dieterbaier.letterwright.usecases.in.ListResult;
import dieterbaier.letterwright.usecases.out.Permissions;
import dieterbaier.letterwright.usecases.out.LetterRepository;

/**
 * Asks the host before the repository: without permission, nothing is read and nothing is counted. The host grants
 * permission per reference; the repository filters out logically deleted letters before paginating (QS-009).
 */
public final class ListLettersService implements ListLetters {

    private final Permissions permissions;
    private final LetterRepository letters;

    public ListLettersService(Permissions permissions, LetterRepository letters) {
        this.permissions = permissions;
        this.letters = letters;
    }

    @Override
    public ListResult list(UserId user, BusinessReference reference, PageRequest page) {
        if (!permissions.may(user, LetterAction.VIEW, reference)) {
            return new ListResult.NotAllowed();
        }
        return new ListResult.Found(letters.visibleFor(reference, page));
    }
}
