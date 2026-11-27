package dieterbaier.letterwright.domain.model;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * The domain object of the host application for which a letter is created, as type and ID (e.g. customer 4711). The
 * library does not interpret either; it only hands them back to the host.
 *
 * <p>The library still compares references by equality: in the list, when deleting and for permissions. It also stores
 * the reference permanently with every letter. This only holds because the host assigns the ID canonically and stably
 * for good and encodes several keys in it itself if needed; the contract is stated by {@code spi.BusinessReference}.
 * There is therefore deliberately no structured key.
 */
@ValueObject
public record BusinessReference(String type, String id) {

    public BusinessReference {
        Validate.notBlank(type, "The type of the business reference");
        Validate.notBlank(id, "The ID of the business reference");
    }
}
