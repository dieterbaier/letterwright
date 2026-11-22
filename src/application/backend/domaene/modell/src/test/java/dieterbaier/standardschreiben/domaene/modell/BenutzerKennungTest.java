package dieterbaier.standardschreiben.domaene.modell;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class BenutzerKennungTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void lehntFehlendeLeereUndBlankeWerteAb(String wert) {
        lehntAb(() -> new BenutzerKennung(wert), "Benutzerkennung");
    }

    @Test
    void istWertgleich() {
        assertThat(new BenutzerKennung("erika")).isEqualTo(new BenutzerKennung("erika"))
                .isNotEqualTo(new BenutzerKennung("max"));
    }
}
