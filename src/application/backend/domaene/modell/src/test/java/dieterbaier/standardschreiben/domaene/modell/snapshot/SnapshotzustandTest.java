package dieterbaier.standardschreiben.domaene.modell.snapshot;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.Testdaten;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SnapshotzustandTest {

    private final Snapshot snapshot = Testdaten.snapshot();

    @Test
    void erkenntEinenGeaendertenDatenkontext() {
        var offen = (Snapshotzustand.Offen) snapshot.zustand();

        assertThat(offen.istUnveraendert(Datenkontext.aus(Map.of("name", "Erika")))).isTrue();
        assertThat(offen.istUnveraendert(Datenkontext.aus(Map.of("name", "Max")))).isFalse();
    }

    @Test
    void einOffenerSnapshotHaeltEinenDatenkontext() {
        lehntAb(() -> new Snapshotzustand.Offen(null), "Datenkontext des Snapshots");
    }
}
