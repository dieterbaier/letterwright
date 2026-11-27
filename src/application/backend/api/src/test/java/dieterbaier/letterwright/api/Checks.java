package dieterbaier.letterwright.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

/** Shared assertion for the error contract of the API. */
final class Checks {

    private Checks() {
    }

    /** The call fails with exactly an IllegalArgumentException that names the error, not with an NPE. */
    static void rejects(ThrowingCallable invocation, String messagePart) {
        assertThatThrownBy(invocation).isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(messagePart);
    }
}
