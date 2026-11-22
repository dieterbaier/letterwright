package dieterbaier.standardschreiben.spi;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

/** Gemeinsame Zusicherung für den Fehlervertrag des SPI. */
final class Pruefungen {

    private Pruefungen() {
    }

    /** Der Aufruf scheitert mit genau einer IllegalArgumentException, die den Fehler benennt, nicht mit einer NPE. */
    static void lehntAb(ThrowingCallable aufruf, String meldungsteil) {
        assertThatThrownBy(aufruf).isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(meldungsteil);
    }
}
