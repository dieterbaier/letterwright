package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/** How a letter is deleted. The host application defines deletion and retention rules (RB-10, ADR-007). */
@ValueObject
public sealed interface DeletionDecision {

    /** The letter and its PDF are removed. */
    static DeletionDecision hard() {
        return new Hard();
    }

    /** The letter is marked as deleted and no longer shown; the PDF is kept. */
    static DeletionDecision logical() {
        return new Logical();
    }

    /** The letter must not be deleted; the reason is shown to the user. */
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
            Validate.notBlank(reason, "The reason for the deletion block");
        }
    }
}
