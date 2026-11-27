package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/** Sequential number of a template version, starting at 1. */
@ValueObject
public record VersionNumber(int value) implements Comparable<VersionNumber> {

    public static final VersionNumber FIRST = new VersionNumber(1);

    public VersionNumber {
        Validate.atLeast(value, 1, "The VersionNumber");
    }

    public VersionNumber next() {
        return new VersionNumber(Math.addExact(value, 1));
    }

    @Override
    public int compareTo(VersionNumber other) {
        return Integer.compare(value, other.value);
    }
}
