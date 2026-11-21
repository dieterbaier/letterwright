package dieterbaier.standardschreiben.domaene.modell;

import java.time.Instant;
import java.util.Map;

/** Gemeinsame Testdaten der Modelltests. Keine Testklasse greift auf eine andere zu, nur auf diese hier. */
final class Testdaten {

    static final Instant JETZT = Instant.parse("2026-11-21T10:00:00Z");
    static final BenutzerKennung SACHBEARBEITER = new BenutzerKennung("erika");
    static final FachlicherBezug KUNDE = new FachlicherBezug("Kunde", "4711");
    static final Versionsverweis BRIEFPAPIER_V1 = new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE);
    static final Versionsverweis STANDARDSCHREIBEN_V3 = new Versionsverweis(VorlagenId.neu(), new Versionsnummer(3));
    static final Datenkontext DATENKONTEXT = Datenkontext.aus(Map.of("name", "Erika"));

    private Testdaten() {
    }

    static Snapshot snapshot() {
        return Snapshot.anlegen(SnapshotId.neu(), SACHBEARBEITER, KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3,
                DATENKONTEXT, JETZT);
    }
}
