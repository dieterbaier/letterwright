package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import java.util.List;

/** The result of a conformance check. */
public sealed interface Conformance {

    static Conformance conformant() {
        return new Conformant();
    }

    static Conformance notConformant(List<String> findings) {
        return new NotConformant(findings);
    }

    record Conformant() implements Conformance {}

    /** At least one finding, so that the user learns what is wrong. */
    record NotConformant(List<String> findings) implements Conformance {

        public NotConformant {
            findings = List.copyOf(Validate.requiredWithoutGaps(findings, "The findings of the conformance check"));
            if (findings.isEmpty()) {
                throw new ValidationException("A non-conformant PDF needs at least one finding");
            }
        }
    }
}
