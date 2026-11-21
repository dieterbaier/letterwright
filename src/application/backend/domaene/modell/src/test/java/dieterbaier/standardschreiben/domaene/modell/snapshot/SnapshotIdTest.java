package dieterbaier.standardschreiben.domaene.modell.snapshot;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SnapshotIdTest {

    @Test
    void dieSnapshotIdVerlangtEinenWertUndWirdNeuVergeben() {
        lehntAb(() -> new SnapshotId(null), "SnapshotId");
        assertThat(SnapshotId.neu()).isNotEqualTo(SnapshotId.neu());
    }
}
