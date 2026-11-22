package dieterbaier.standardschreiben.domaene.modell.schreiben;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SchreibenIdTest {

    @Test
    void dieSchreibenIdVerlangtEinenWertUndLaesstSichWiederherstellen() {
        UUID wert = UUID.randomUUID();

        lehntAb(() -> new SchreibenId(null), "SchreibenId");
        assertThat(new SchreibenId(wert)).isEqualTo(new SchreibenId(wert));
        assertThat(SchreibenId.neu()).isNotEqualTo(SchreibenId.neu());
    }
}
