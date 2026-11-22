package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreibenzustand;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.time.Instant;

/** Ein gespeichertes Schreiben für die Tests der Ergebnistypen. */
final class Testschreiben {

    /** Nur lesen; wer es ändern will, nimmt eine Kopie. */
    static final byte[] PDF = {1, 2, 3};
    static final Schreiben SCHREIBEN = Schreiben.wiederherstellen(SchreibenId.neu(), SnapshotId.neu(),
            new FachlicherBezug("Kunde", "4711"), new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE),
            new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE), new Dokumentverweis("ablage/1"),
            Pruefsumme.ueber(PDF), Instant.parse("2026-11-22T10:00:00Z"), new Schreibenzustand.Sichtbar());

    private Testschreiben() {
    }
}
