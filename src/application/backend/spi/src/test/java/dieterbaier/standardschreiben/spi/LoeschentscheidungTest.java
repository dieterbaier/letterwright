package dieterbaier.standardschreiben.spi;

import static dieterbaier.standardschreiben.spi.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class LoeschentscheidungTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void eineLoeschsperreBrauchtEinenGrund(String grund) {
        lehntAb(() -> Loeschentscheidung.gesperrt(grund), "Der Grund der Löschsperre");
    }

    @Test
    void loeschentscheidungenSindWerte() {
        assertThat(Loeschentscheidung.hart()).isEqualTo(new Loeschentscheidung.Hart());
        assertThat(Loeschentscheidung.logisch()).isEqualTo(new Loeschentscheidung.Logisch());
        assertThat(Loeschentscheidung.gesperrt("Frist")).isEqualTo(new Loeschentscheidung.Gesperrt("Frist"));
    }
}
