package dieterbaier.letterwright.spi;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

/** Gemeinsame Zusicherung für den Fehlervertrag des SPI. */
final class Checks {

    private Checks() {
    }

    /** Der Aufruf scheitert mit genau einer IllegalArgumentException, die den Fehler benennt, nicht mit einer NPE. */
    static void rejects(ThrowingCallable invocation, String messagePart) {
        assertThatThrownBy(invocation).isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(messagePart);
    }
}
