package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.time.Instant;
import java.util.Map;

/** Gemeinsame Testdaten der Modelltests. Keine Testklasse greift auf eine andere zu, nur auf diese hier. */
public final class Testdaten {

    public static final Instant JETZT = Instant.parse("2026-11-21T10:00:00Z");
    public static final BenutzerKennung SACHBEARBEITER = new BenutzerKennung("erika");
    public static final FachlicherBezug KUNDE = new FachlicherBezug("Kunde", "4711");
    public static final Versionsverweis BRIEFPAPIER_V1 = new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE);
    public static final Versionsverweis STANDARDSCHREIBEN_V3 = new Versionsverweis(VorlagenId.neu(), new Versionsnummer(3));
    public static final Datenkontext DATENKONTEXT = Datenkontext.aus(Map.of("name", "Erika"));

    private Testdaten() {
    }

    public static Snapshot snapshot() {
        return Snapshot.anlegen(SnapshotId.neu(), SACHBEARBEITER, KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3,
                DATENKONTEXT, JETZT);
    }
}
