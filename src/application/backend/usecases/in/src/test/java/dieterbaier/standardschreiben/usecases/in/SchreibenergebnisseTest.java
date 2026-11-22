package dieterbaier.standardschreiben.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.Seite;
import dieterbaier.standardschreiben.domaene.modell.Seitenanfrage;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreibenzustand;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Die Ergebnisse von Auflisten, Herunterladen und Löschen, wie ein eingehender Adapter sie unterscheidet. */
class SchreibenergebnisseTest {

    private final byte[] pdf = {1, 2, 3};
    private final Schreiben schreiben = Schreiben.wiederherstellen(SchreibenId.neu(), SnapshotId.neu(),
            new FachlicherBezug("Kunde", "4711"), new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE),
            new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE), new Dokumentverweis("ablage/1"),
            Pruefsumme.ueber(pdf), Instant.parse("2026-11-22T10:00:00Z"), new Schreibenzustand.Sichtbar());

    /** Der Compiler prüft, dass kein Fall fehlt; der Status ist nur ein Beispiel für einen Adapter. */
    @Test
    void jedesErgebnisIstUnterscheidbar() {
        var seite = new Seite<>(List.of(schreiben), 1, new Seitenanfrage(0, 20));
        assertThat(Stream.of(new Uebersichtsergebnis.Gefunden(seite), new Uebersichtsergebnis.NichtErlaubt())
                .map(ergebnis -> switch (ergebnis) {
                    case Uebersichtsergebnis.Gefunden gefunden -> 200;
                    case Uebersichtsergebnis.NichtErlaubt nichtErlaubt -> 403;
                })).containsExactly(200, 403);
        assertThat(Stream.of(new Downloadergebnis.Geliefert(schreiben, pdf), new Downloadergebnis.Unbekannt())
                .map(ergebnis -> switch (ergebnis) {
                    case Downloadergebnis.Geliefert geliefert -> 200;
                    case Downloadergebnis.Unbekannt unbekannt -> 404;
                })).containsExactly(200, 404);
        assertThat(Stream.of(new Loeschergebnis.Geloescht(), new Loeschergebnis.Gesperrt("Frist"),
                new Loeschergebnis.Unbekannt())
                .map(ergebnis -> switch (ergebnis) {
                    case Loeschergebnis.Geloescht geloescht -> 204;
                    case Loeschergebnis.Gesperrt gesperrt -> 409;
                    case Loeschergebnis.Unbekannt unbekannt -> 404;
                })).containsExactly(204, 409, 404);
    }

    @Test
    void dasGeliefertePdfLaesstSichNichtVonAussenAendern() {
        var geliefert = new Downloadergebnis.Geliefert(schreiben, pdf);
        pdf[0] = 9;
        geliefert.pdf()[1] = 9;

        assertThat(geliefert.pdf()).containsExactly(1, 2, 3);
        assertThat(geliefert.schreiben()).isEqualTo(schreiben);
    }

    @Test
    void pflichtattribute() {
        assertThat(new Loeschergebnis.Gesperrt("Frist").grund()).isEqualTo("Frist");
        assertThatThrownBy(() -> new Uebersichtsergebnis.Gefunden(null)).hasMessageContaining("Seite");
        assertThatThrownBy(() -> new Downloadergebnis.Geliefert(null, pdf)).hasMessageContaining("Schreiben");
        assertThatThrownBy(() -> new Downloadergebnis.Geliefert(schreiben, null)).hasMessageContaining("PDF");
        assertThatThrownBy(() -> new Loeschergebnis.Gesperrt(" ")).hasMessageContaining("Grund");
    }
}
