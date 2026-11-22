package dieterbaier.standardschreiben.adapter.out.host;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** Gemeinsame Testdaten der Adaptertests. */
final class Testdaten {

    static final BenutzerKennung ERIKA = new BenutzerKennung("erika");
    static final FachlicherBezug KUNDE = new FachlicherBezug("Kunde", "4711");
    static final Instant ERSTELLT = Instant.parse("2026-11-21T10:00:00Z");
    static final Standardschreiben MAHNUNG =
            Standardschreiben.anlegen(VorlagenId.neu(), "Mahnung", VorlagenId.neu(), "Kunde", "Sehr geehrte {{name}}");
    static final Schreiben SCHREIBEN = Schreiben.wiederherstellen(SchreibenId.neu(), KUNDE,
            new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE),
            new Versionsverweis(MAHNUNG.id(), Versionsnummer.ERSTE), new Dokumentverweis("ablage/1.pdf"),
            Pruefsumme.ueber("pdf".getBytes(StandardCharsets.UTF_8)), ERSTELLT);

    private Testdaten() {
    }
}
