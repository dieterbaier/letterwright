package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import java.util.List;

/** Ergebnis einer Konformitätsprüfung. */
public sealed interface Conformance {

    static Conformance conformant() {
        return new Conformant();
    }

    static Conformance notConformant(List<String> findings) {
        return new NotConformant(findings);
    }

    record Conformant() implements Conformance {}

    /** Mindestens ein Befund, damit der Benutzer erfährt, was nicht stimmt. */
    record NotConformant(List<String> findings) implements Conformance {

        public NotConformant {
            findings = List.copyOf(Validate.requiredWithoutGaps(findings, "Die Befunde der Konformitätsprüfung"));
            if (findings.isEmpty()) {
                throw new ValidationException("Ein nicht konformes PDF braucht mindestens einen Befund");
            }
        }
    }
}
