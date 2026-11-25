package dieterbaier.standardschreiben.spi;

import static dieterbaier.standardschreiben.spi.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class RueckrufergebnisTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void eineAblehnungBrauchtEinenGrund(String grund) {
        lehntAb(() -> Rueckrufergebnis.abgelehnt(grund), "Der Grund der Ablehnung");
    }

    @Test
    void rueckrufergebnisseSindWerte() {
        assertThat(Rueckrufergebnis.angenommen()).isEqualTo(new Rueckrufergebnis.Angenommen());
        assertThat(Rueckrufergebnis.abgelehnt("Aufgabe ist bereits erledigt"))
                .isEqualTo(new Rueckrufergebnis.Abgelehnt("Aufgabe ist bereits erledigt"));
    }
}
