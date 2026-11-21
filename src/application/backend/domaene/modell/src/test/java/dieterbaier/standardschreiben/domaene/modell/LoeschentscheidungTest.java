package dieterbaier.standardschreiben.domaene.modell;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class LoeschentscheidungTest {

    @Test
    void eineSperreTraegtIhrenGrund() {
        assertThat(Loeschentscheidung.gesperrt("Aufbewahrungsfrist bis 2036"))
                .isEqualTo(new Loeschentscheidung.Gesperrt("Aufbewahrungsfrist bis 2036"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void eineSperreOhneGrundIstUnzulaessig(String grund) {
        lehntAb(() -> Loeschentscheidung.gesperrt(grund), "Grund der Löschsperre");
    }

    @Test
    void hartUndLogischSindWerte() {
        assertThat(Loeschentscheidung.hart()).isEqualTo(new Loeschentscheidung.Hart());
        assertThat(Loeschentscheidung.logisch()).isEqualTo(new Loeschentscheidung.Logisch());
        assertThat(Loeschentscheidung.hart()).isNotEqualTo(Loeschentscheidung.logisch());
    }
}
