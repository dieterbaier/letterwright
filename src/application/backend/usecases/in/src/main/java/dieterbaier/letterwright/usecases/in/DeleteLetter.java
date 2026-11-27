package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.LetterId;

/**
 * Anwendungsfall „Schreiben löschen“: Ob und wie gelöscht wird, entscheidet die Löschrichtlinie des Hosts (RB-10,
 * ADR-007). Ein Schreiben, das der Benutzer nicht löschen darf, gibt es für ihn nicht (QS-009).
 */
public interface DeleteLetter {

    DeletionResult delete(UserId user, LetterId letter);
}
