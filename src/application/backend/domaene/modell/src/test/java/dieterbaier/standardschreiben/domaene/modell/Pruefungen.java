package dieterbaier.standardschreiben.domaene.modell;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.validierung.Validierungsfehler;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

/** Gemeinsame Zusicherung für den Fehlervertrag des Modells. */
public final class Pruefungen {

    private Pruefungen() {
    }

    /** Der Aufruf scheitert mit einem Validierungsfehler, der das Attribut nennt, nicht mit einer NPE. */
    public static void lehntAb(ThrowingCallable aufruf, String attribut) {
        assertThatThrownBy(aufruf).isInstanceOf(Validierungsfehler.class).hasMessageContaining(attribut);
    }
}
