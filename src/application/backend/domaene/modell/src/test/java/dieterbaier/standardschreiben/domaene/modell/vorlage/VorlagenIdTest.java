package dieterbaier.standardschreiben.domaene.modell.vorlage;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VorlagenIdTest {

    @Test
    void dieVorlagenIdVerlangtEinenWertUndWirdNeuVergeben() {
        lehntAb(() -> new VorlagenId(null), "VorlagenId");
        assertThat(VorlagenId.neu()).isNotEqualTo(VorlagenId.neu());
    }
}
