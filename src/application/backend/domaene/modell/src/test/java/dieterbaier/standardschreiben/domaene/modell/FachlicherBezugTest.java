package dieterbaier.standardschreiben.domaene.modell;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class FachlicherBezugTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void typUndIdLehnenFehlendeLeereUndBlankeWerteAb(String wert) {
        lehntAb(() -> new FachlicherBezug(wert, "4711"), "Typ des fachlichen Bezugs");
        lehntAb(() -> new FachlicherBezug("Kunde", wert), "ID des fachlichen Bezugs");
    }

    @Test
    void istWertgleich() {
        assertThat(new FachlicherBezug("Kunde", "4711")).isEqualTo(new FachlicherBezug("Kunde", "4711"))
                .isNotEqualTo(new FachlicherBezug("Vorgang", "4711"));
    }
}
