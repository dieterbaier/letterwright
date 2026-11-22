package dieterbaier.standardschreiben.spi;

import static dieterbaier.standardschreiben.spi.Pruefungen.lehntAb;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class StandardschreibenkennungTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void derNameLehntFehlendeLeereUndBlankeWerteAb(String wert) {
        lehntAb(() -> new Standardschreibenkennung(UUID.randomUUID(), wert), "Der Name des Standardschreibens");
    }

    @Test
    void dieIdIstPflicht() {
        lehntAb(() -> new Standardschreibenkennung(null, "Mahnung"), "Die ID des Standardschreibens");
    }
}
