package dieterbaier.letterwright.domain.model;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/** Which page of a list is requested. The first page has number 0. */
@ValueObject
public record PageRequest(int number, int size) {

    public PageRequest {
        Validate.atLeast(number, 0, "The page number");
        Validate.atLeast(size, 1, "The page size");
    }

    /** How many entries precede this page. */
    public long offset() {
        return (long) number * size;
    }
}
