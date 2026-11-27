package dieterbaier.letterwright.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.validation.ValidationException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

/** Gemeinsame Zusicherung für den Fehlervertrag des Modells. */
public final class Checks {

    private Checks() {
    }

    /** Der Aufruf scheitert mit einem Validierungsfehler, der das Attribut nennt, nicht mit einer NPE. */
    public static void rejects(ThrowingCallable invocation, String attribute) {
        assertThatThrownBy(invocation).isInstanceOf(ValidationException.class).hasMessageContaining(attribute);
    }
}
