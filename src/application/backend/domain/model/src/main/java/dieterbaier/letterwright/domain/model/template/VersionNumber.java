package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/** Laufende Nummer einer Vorlagenversion, beginnend bei 1. */
@ValueObject
public record VersionNumber(int value) implements Comparable<VersionNumber> {

    public static final VersionNumber FIRST = new VersionNumber(1);

    public VersionNumber {
        Validate.atLeast(value, 1, "Die Versionsnummer");
    }

    public VersionNumber next() {
        return new VersionNumber(Math.addExact(value, 1));
    }

    @Override
    public int compareTo(VersionNumber others) {
        return Integer.compare(value, others.value);
    }
}
