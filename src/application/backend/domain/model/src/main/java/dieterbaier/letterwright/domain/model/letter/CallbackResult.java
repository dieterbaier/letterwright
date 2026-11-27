package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * How the host application responds to a newly created letter if it provides the optional letter save callback
 * (ADR-023). Without a callback every letter counts as accepted.
 */
@ValueObject
public sealed interface CallbackResult {

    static CallbackResult accepted() {
        return new Accepted();
    }

    /** The host rejects; the reason is shown to the user. */
    static CallbackResult rejected(String reason) {
        return new Rejected(reason);
    }

    @ValueObject
    record Accepted() implements CallbackResult {
    }

    @ValueObject
    record Rejected(String reason) implements CallbackResult {

        public Rejected {
            Validate.notBlank(reason, "The reason for the rejection");
        }
    }
}
