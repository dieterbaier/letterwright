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
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.spi.Standardschreibenkennung;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** Gemeinsame Testdaten der Adaptertests. */
final class Testdaten {

    static final BenutzerKennung ERIKA = new BenutzerKennung("erika");
    static final FachlicherBezug KUNDE = new FachlicherBezug("Kunde", "4711");
    static final Instant ERSTELLT = Instant.parse("2026-11-21T10:00:00Z");
    static final Standardschreiben MAHNUNG =
            Standardschreiben.anlegen(VorlagenId.neu(), "Mahnung", VorlagenId.neu(), "Kunde", "Sehr geehrte {{name}}");
    static final Schreiben SCHREIBEN = Schreiben.wiederherstellen(SchreibenId.neu(), SnapshotId.neu(), KUNDE,
            new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE),
            new Versionsverweis(MAHNUNG.id(), Versionsnummer.ERSTE), new Dokumentverweis("ablage/1.pdf"),
            Pruefsumme.ueber("pdf".getBytes(StandardCharsets.UTF_8)), ERSTELLT);

    /**
     * So sehen Benutzer, Bezug und Mahnung im SPI aus. Von Hand aufgebaut und nicht über {@link Abbildung}, damit ein
     * Fehler in der Abbildung in den Adaptertests sichtbar wird.
     */
    static final String ERIKA_IM_SPI = "erika";
    static final dieterbaier.standardschreiben.spi.FachlicherBezug KUNDE_IM_SPI =
            new dieterbaier.standardschreiben.spi.FachlicherBezug("Kunde", "4711");
    static final Standardschreibenkennung MAHNUNG_IM_SPI =
            new Standardschreibenkennung(MAHNUNG.id().wert(), "Mahnung");

    private Testdaten() {
    }

    /** Ein Host, der sich nicht an den Null-Vertrag hält. */
    @SuppressWarnings({"NullAway", "unchecked"})
    static <T> T nullVomHost() {
        return (T) null;
    }
}
