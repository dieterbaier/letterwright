package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Wie die Host-Anwendung auf ein neu angelegtes Schreiben antwortet, wenn sie den optionalen Rückruf nach dem Speichern
 * bereitstellt (ADR-023). Ohne Rückruf gilt jedes Schreiben als angenommen.
 */
@ValueObject
public sealed interface CallbackResult {

    static CallbackResult accepted() {
        return new Accepted();
    }

    /** Der Host lehnt ab; der Grund wird dem Benutzer angezeigt. */
    static CallbackResult rejected(String reason) {
        return new Rejected(reason);
    }

    @ValueObject
    record Accepted() implements CallbackResult {
    }

    @ValueObject
    record Rejected(String reason) implements CallbackResult {

        public Rejected {
            Validate.notBlank(reason, "Der Grund der Ablehnung");
        }
    }
}
