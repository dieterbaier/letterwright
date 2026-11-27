package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/** Wie ein Schreiben gelöscht wird. Lösch- und Aufbewahrungsregeln bestimmt die Host-Anwendung (RB-10, ADR-007). */
@ValueObject
public sealed interface DeletionDecision {

    /** Das Schreiben und sein PDF werden entfernt. */
    static DeletionDecision hard() {
        return new Hard();
    }

    /** Das Schreiben wird als gelöscht markiert und nicht mehr angezeigt; das PDF bleibt erhalten. */
    static DeletionDecision logical() {
        return new Logical();
    }

    /** Das Schreiben darf nicht gelöscht werden; der Grund wird dem Benutzer angezeigt. */
    static DeletionDecision blocked(String reason) {
        return new Blocked(reason);
    }

    @ValueObject
    record Hard() implements DeletionDecision {
    }

    @ValueObject
    record Logical() implements DeletionDecision {
    }

    @ValueObject
    record Blocked(String reason) implements DeletionDecision {

        public Blocked {
            Validate.notBlank(reason, "Der Grund der Löschsperre");
        }
    }
}
