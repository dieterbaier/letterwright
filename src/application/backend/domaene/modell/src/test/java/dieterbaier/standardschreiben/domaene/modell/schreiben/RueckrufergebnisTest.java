package dieterbaier.standardschreiben.domaene.modell.schreiben;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class RueckrufergebnisTest {

    @Test
    void eineAblehnungTraegtIhrenGrund() {
        assertThat(Rueckrufergebnis.abgelehnt("Aufgabe ist bereits erledigt"))
                .isEqualTo(new Rueckrufergebnis.Abgelehnt("Aufgabe ist bereits erledigt"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void eineAblehnungOhneGrundIstUnzulaessig(String grund) {
        lehntAb(() -> Rueckrufergebnis.abgelehnt(grund), "Grund der Ablehnung");
    }

    @Test
    void angenommenIstEinWert() {
        assertThat(Rueckrufergebnis.angenommen()).isEqualTo(new Rueckrufergebnis.Angenommen());
    }
}
