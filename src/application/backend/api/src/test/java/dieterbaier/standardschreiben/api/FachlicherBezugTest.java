package dieterbaier.standardschreiben.api;

import static dieterbaier.standardschreiben.api.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class FachlicherBezugTest {

    @Test
    void traegtTypUndId() {
        var bezug = new FachlicherBezug("Kunde", "4711");

        assertThat(bezug.typ()).isEqualTo("Kunde");
        assertThat(bezug.id()).isEqualTo("4711");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void typUndIdLehnenFehlendeLeereUndBlankeWerteAb(String wert) {
        lehntAb(() -> new FachlicherBezug(wert, "4711"), "Der Typ des fachlichen Bezugs");
        lehntAb(() -> new FachlicherBezug("Kunde", wert), "Die ID des fachlichen Bezugs");
    }
}
