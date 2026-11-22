package dieterbaier.standardschreiben.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.Seitenanfrage;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Loeschentscheidung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreibenzustand;
import dieterbaier.standardschreiben.usecases.in.Loeschergebnis;
import dieterbaier.standardschreiben.usecases.in.Uebersichtsergebnis;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Brücke zu {@code features/schreiben/schreiben-loeschen.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die ausgehenden Ports sind {@link Attrappen}.
 */
class SchreibenLoeschenTest {

    private static final Instant GESPEICHERT = Instant.parse("2026-11-20T09:00:00Z");
    private static final Instant JETZT = Instant.parse("2026-11-22T11:00:00Z");
    private static final BenutzerKennung ERIKA = new BenutzerKennung("erika");
    private static final BenutzerKennung MAX = new BenutzerKennung("max");
    private static final FachlicherBezug KUNDE_4711 = new FachlicherBezug("Kunde", "4711");

    private final Attrappen.Host host = new Attrappen.Host();
    private final Attrappen.Loeschrichtlinie richtlinie = new Attrappen.Loeschrichtlinie();
    private final Attrappen.Dokumente dokumente = new Attrappen.Dokumente();
    private final Attrappen.SchreibenAblage schreiben = new Attrappen.SchreibenAblage(new Attrappen.Snapshots());
    private final GespeicherteSchreiben gespeicherte = new GespeicherteSchreiben(schreiben, dokumente);
    private final Schreibenloeschung loeschung =
            new Schreibenloeschung(schreiben, host, richtlinie, dokumente, new Attrappen.Uhr(JETZT));

    // Grundlage: ein Schreiben zum Kunden 4711; Erika darf löschen.
    private final Schreiben mahnung = gespeicherte.speichere(KUNDE_4711, GESPEICHERT);

    {
        host.erlaube(ERIKA, SchreibenAktion.LOESCHEN, KUNDE_4711);
    }

    @Test
    void hartGeloeschtSindSchreibenUndPdfEntfernt() {
        richtlinie.entscheidung = Loeschentscheidung.hart();

        var ergebnis = loeschung.loesche(ERIKA, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Loeschergebnis.Geloescht());
        assertThat(schreiben.mitId(mahnung.id())).as("Schreiben entfernt").isEmpty();
        assertThat(dokumente.abgelegt).as("PDF entfernt").doesNotContainKey(mahnung.dokument());
        assertThat(richtlinie.angefragt).containsExactly(mahnung);
    }

    @Test
    void logischGeloeschtBleibtDasPdfDasSchreibenIstNichtMehrSichtbar() {
        richtlinie.entscheidung = Loeschentscheidung.logisch();

        var ergebnis = loeschung.loesche(ERIKA, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Loeschergebnis.Geloescht());
        assertThat(schreiben.mitId(mahnung.id()).orElseThrow().zustand())
                .isEqualTo(new Schreibenzustand.LogischGeloescht(JETZT));
        assertThat(dokumente.abgelegt).containsKey(mahnung.dokument());
        host.erlaube(ERIKA, SchreibenAktion.ANSEHEN, KUNDE_4711);
        var uebersicht = (Uebersichtsergebnis.Gefunden) new Schreibenuebersicht(host, schreiben)
                .liste(ERIKA, KUNDE_4711, new Seitenanfrage(0, 20));
        assertThat(uebersicht.seite().eintraege()).isEmpty();
        assertThat(uebersicht.seite().gesamtzahl()).isZero();
    }

    @Test
    void eineGesperrteLoeschungNenntDenGrund() {
        richtlinie.entscheidung = Loeschentscheidung.gesperrt("Aufbewahrungsfrist bis 2036");

        var ergebnis = loeschung.loesche(ERIKA, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Loeschergebnis.Gesperrt("Aufbewahrungsfrist bis 2036"));
        schreibenUnveraendert();
    }

    @Test
    void einNichtErlaubtesSchreibenVerhaeltSichWieEinNichtVorhandenes() {
        var ergebnis = loeschung.loesche(MAX, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Loeschergebnis.Unbekannt());
        assertThat(richtlinie.angefragt).as("Löschrichtlinie nicht gefragt").isEmpty();
        schreibenUnveraendert();
    }

    @Test
    void einUnbekanntesSchreibenGibtEsNicht() {
        var ergebnis = loeschung.loesche(ERIKA, SchreibenId.neu());

        assertThat(ergebnis).isEqualTo(new Loeschergebnis.Unbekannt());
        assertThat(host.wurdeGefragt()).isFalse();
        assertThat(richtlinie.angefragt).isEmpty();
    }

    @Test
    void einLogischGeloeschtesSchreibenWirdNichtErneutGeloescht() {
        Schreiben geloescht = gespeicherte.loescheLogisch(mahnung, GESPEICHERT.plusSeconds(60));

        var ergebnis = loeschung.loesche(ERIKA, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Loeschergebnis.Unbekannt());
        assertThat(host.wurdeGefragt()).isFalse();
        assertThat(richtlinie.angefragt).isEmpty();
        assertThat(schreiben.mitId(mahnung.id()).orElseThrow().zustand()).isEqualTo(geloescht.zustand());
    }

    private void schreibenUnveraendert() {
        Schreiben gespeichert = schreiben.mitId(mahnung.id()).orElseThrow();
        assertThat(gespeichert.zustand()).isEqualTo(new Schreibenzustand.Sichtbar());
        assertThat(dokumente.abgelegt).containsKey(mahnung.dokument());
    }
}
