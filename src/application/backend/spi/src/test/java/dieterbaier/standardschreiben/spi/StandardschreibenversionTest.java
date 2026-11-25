package dieterbaier.standardschreiben.spi;

import static dieterbaier.standardschreiben.spi.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StandardschreibenversionTest {

    private final UUID mahnung = UUID.randomUUID();

    @Test
    void dieErsteVersionHatDieNummerEins() {
        assertThat(new Standardschreibenversion(mahnung, 1).nummer()).isEqualTo(1);
    }

    @Test
    void lehntEineFehlendeIdAb() {
        lehntAb(() -> new Standardschreibenversion(null, 1), "Die ID des Standardschreibens");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void lehntEineNummerUnterEinsAb(int nummer) {
        lehntAb(() -> new Standardschreibenversion(mahnung, nummer), "Die Versionsnummer");
    }
}
