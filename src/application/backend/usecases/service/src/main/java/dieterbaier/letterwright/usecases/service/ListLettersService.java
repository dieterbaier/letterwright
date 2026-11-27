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
 * Fragt den Host vor dem Repository: Ohne Berechtigung wird nichts gelesen und nichts gezählt. Der Host erteilt sie je
 * Bezug; logisch gelöschte Schreiben filtert das Repository vor dem Paginieren (QS-009).
 */
public final class ListLettersService implements ListLetters {

    private final Permissions permissions;
    private final LetterRepository letter;

    public ListLettersService(Permissions permissions, LetterRepository letter) {
        this.permissions = permissions;
        this.letter = letter;
    }

    @Override
    public ListResult list(UserId user, BusinessReference reference, PageRequest page) {
        if (!permissions.may(user, LetterAction.VIEW, reference)) {
            return new ListResult.NotAllowed();
        }
        return new ListResult.Found(letter.visibleFor(reference, page));
    }
}
