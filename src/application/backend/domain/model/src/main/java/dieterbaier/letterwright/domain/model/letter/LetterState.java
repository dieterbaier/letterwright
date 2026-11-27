package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.validation.Validate;
import java.time.Instant;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Ein Schreiben ist sichtbar, bis es logisch gelöscht wird. Ein logisch gelöschtes Schreiben behält sein PDF, erscheint
 * aber in keiner Übersicht und lässt sich weder herunterladen noch erneut löschen; für den Benutzer gibt es es nicht
 * mehr. Ein hart gelöschtes Schreiben hat keinen Zustand, es ist entfernt.
 */
@ValueObject
public sealed interface LetterState {

    record Visible() implements LetterState {}

    record LogicallyDeleted(Instant at) implements LetterState {

        public LogicallyDeleted {
            Validate.required(at, "Der Zeitpunkt des Löschens");
        }
    }
}
