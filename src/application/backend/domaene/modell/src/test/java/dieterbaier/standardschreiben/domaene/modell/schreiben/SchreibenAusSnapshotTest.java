package dieterbaier.standardschreiben.domaene.modell.schreiben;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.JETZT;

import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.Testdaten;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SchreibenAusSnapshotTest {

    private final Snapshot snapshot = Testdaten.snapshot();
    private final Dokumentverweis dokument = new Dokumentverweis("ablage/1");
    private final Pruefsumme pdf = Pruefsumme.ueber("%PDF-1.7".getBytes(StandardCharsets.US_ASCII));

    @Test
    void pflichtattributeDesErgebnisses() {
        Schreiben schreiben = Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, JETZT).schreiben();

        lehntAb(() -> new SchreibenAusSnapshot(null, snapshot), "Das Schreiben");
        lehntAb(() -> new SchreibenAusSnapshot(schreiben, null), "verbrauchte Snapshot");
    }

    @Test
    void dasErgebnisPasstZusammen() {
        SchreibenAusSnapshot erzeugt = Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, JETZT);
        Snapshot andererVerbraucht = Schreiben.ausSnapshot(SchreibenId.neu(), Testdaten.snapshot(), dokument, pdf, JETZT)
                .verbrauchterSnapshot();

        lehntAb(() -> new SchreibenAusSnapshot(erzeugt.schreiben(), snapshot), "nicht verbraucht");
        lehntAb(() -> new SchreibenAusSnapshot(erzeugt.schreiben(), andererVerbraucht), "entstand nicht aus Snapshot");
    }
}
