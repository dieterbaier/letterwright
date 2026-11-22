package dieterbaier.standardschreiben.spi;

import static dieterbaier.standardschreiben.spi.Pruefungen.lehntAb;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class FachlicherBezugTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void typUndIdLehnenFehlendeLeereUndBlankeWerteAb(String wert) {
        lehntAb(() -> new FachlicherBezug(wert, "4711"), "Der Typ des fachlichen Bezugs");
        lehntAb(() -> new FachlicherBezug("Kunde", wert), "Die ID des fachlichen Bezugs");
    }
}
