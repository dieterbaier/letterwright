package dieterbaier.standardschreiben.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.usecases.in.Downloadergebnis;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Brücke zu {@code features/schreiben/schreiben-herunterladen.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die ausgehenden Ports sind {@link Attrappen}.
 */
class SchreibenHerunterladenTest {

    private static final Instant GESPEICHERT = Instant.parse("2026-11-20T09:00:00Z");
    private static final BenutzerKennung ERIKA = new BenutzerKennung("erika");
    private static final BenutzerKennung MAX = new BenutzerKennung("max");
    private static final FachlicherBezug KUNDE_4711 = new FachlicherBezug("Kunde", "4711");

    private final Attrappen.Host host = new Attrappen.Host();
    private final Attrappen.Dokumente dokumente = new Attrappen.Dokumente();
    private final Attrappen.SchreibenAblage schreiben = new Attrappen.SchreibenAblage(new Attrappen.Snapshots());
    private final GespeicherteSchreiben gespeicherte = new GespeicherteSchreiben(schreiben, dokumente);
    private final Schreibenabruf abruf = new Schreibenabruf(schreiben, host, dokumente);

    // Grundlage: ein Schreiben zum Kunden 4711; Erika darf herunterladen.
    private final Schreiben mahnung = gespeicherte.speichere(KUNDE_4711, GESPEICHERT);

    {
        host.erlaube(ERIKA, SchreibenAktion.HERUNTERLADEN, KUNDE_4711);
    }

    @Test
    void derDownloadLiefertDasUnveraendertePdf() {
        var ergebnis = (Downloadergebnis.Geliefert) abruf.lade(ERIKA, mahnung.id());

        assertThat(Pruefsumme.ueber(ergebnis.pdf())).isEqualTo(mahnung.pdfPruefsumme());
        assertThat(ergebnis.pdf()).isEqualTo(dokumente.abgelegt.get(mahnung.dokument()));
        assertThat(ergebnis.schreiben()).isEqualTo(mahnung);
        assertThat(host.schreibenAktionAngefragt)
                .containsExactly(new Attrappen.Erlaubnis(ERIKA, SchreibenAktion.HERUNTERLADEN, KUNDE_4711));
    }

    @Test
    void einNichtErlaubtesSchreibenVerhaeltSichWieEinNichtVorhandenes() {
        var ergebnis = abruf.lade(MAX, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Downloadergebnis.Unbekannt());
        assertThat(dokumente.gelesen).as("kein PDF gelesen").isZero();
    }

    @Test
    void einUnbekanntesSchreibenGibtEsNicht() {
        var ergebnis = abruf.lade(ERIKA, SchreibenId.neu());

        assertThat(ergebnis).isEqualTo(new Downloadergebnis.Unbekannt());
        assertThat(host.wurdeGefragt()).isFalse();
    }

    @Test
    void einLogischGeloeschtesSchreibenGibtEsNicht() {
        gespeicherte.loescheLogisch(mahnung, GESPEICHERT.plusSeconds(60));

        var ergebnis = abruf.lade(ERIKA, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Downloadergebnis.Unbekannt());
        assertThat(host.wurdeGefragt()).isFalse();
        assertThat(dokumente.gelesen).isZero();
    }

    @Test
    void einVeraendertesPdfWirdNichtAusgeliefert() {
        dokumente.abgelegt.put(mahnung.dokument(), "%PDF verändert".getBytes(StandardCharsets.UTF_8));

        assertThatIllegalStateException().isThrownBy(() -> abruf.lade(ERIKA, mahnung.id()))
                .withMessageContaining("Prüfsumme")
                .withMessageNotContaining("4711");
    }
}
