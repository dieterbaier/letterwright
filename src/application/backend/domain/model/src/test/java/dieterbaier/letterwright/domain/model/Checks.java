package dieterbaier.letterwright.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.validation.ValidationException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

/** Shared assertion for the error contract of the model. */
public final class Checks {

    private Checks() {
    }

    /** The invocation fails with a validation error that names the attribute, not with an NPE. */
    public static void rejects(ThrowingCallable invocation, String attribute) {
        assertThatThrownBy(invocation).isInstanceOf(ValidationException.class).hasMessageContaining(attribute);
    }
}
