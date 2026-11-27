package dieterbaier.letterwright.domain.model;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * ID of the signed-in user as supplied by the host application. The library does not manage users; it only compares
 * IDs.
 */
@ValueObject
public record UserId(String value) {

    public UserId {
        Validate.notBlank(value, "The user ID");
    }
}
