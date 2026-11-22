package dieterbaier.standardschreiben.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Briefpapier;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.UnzulaessigerZustandswechsel;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenversion;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Vorlagenzustand;
import dieterbaier.standardschreiben.usecases.in.Veroeffentlichungsergebnis;
import dieterbaier.standardschreiben.usecases.out.Konformitaet;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Brücke zu {@code features/vorlagen/vorlagenversion-veroeffentlichen.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die ausgehenden Ports sind {@link Attrappen}.
 */
class VorlagenveroeffentlichungTest {

    private static final BenutzerKennung VERA = new BenutzerKennung("vera");
    private static final BenutzerKennung MAX = new BenutzerKennung("max");
    private static final Versionsnummer ZWEI = new Versionsnummer(2);

    private final Attrappen.Vorlagen vorlagen = new Attrappen.Vorlagen();
    private final Attrappen.Host host = new Attrappen.Host();
    private final Attrappen.Renderer renderer = new Attrappen.Renderer();
    private final Attrappen.Pruefer pruefer = new Attrappen.Pruefer();
    private final Vorlagenveroeffentlichung veroeffentlichung =
            new Vorlagenveroeffentlichung(vorlagen, host, renderer, pruefer);

    // Grundlage: Briefpapier "Standard" V1 veröffentlicht mit Beispieldaten; "Mahnung" V1 veröffentlicht, Entwurf V2
    // mit eigenen Beispieldaten; Vera darf veröffentlichen.
    private final Briefpapier briefpapier = vorlagen.ablegen(Briefpapier.anlegenMitStandardtyp(VorlagenId.neu(),
            "Standard", "<header/>").aendereEntwurf("<header/>",
                    Datenkontext.aus(Map.of("absender", "Muster GmbH", "betreff", "Allgemein")))
            .veroeffentlicheEntwurf());
    private final Standardschreiben mahnung = vorlagen.ablegen(Standardschreiben.anlegen(VorlagenId.neu(), "Mahnung",
            briefpapier.id(), "Kunde", "<p>{{name}}</p>").veroeffentlicheEntwurf()
            .neuerEntwurf("<p>Erneut: {{name}}</p>")
            .aendereEntwurf("<p>Erneut: {{name}}</p>",
                    Datenkontext.aus(Map.of("name", "Erika Mustermann GmbH", "betreff", "Mahnung"))));

    {
        host.duerfenVeroeffentlichen.add(VERA);
    }

    @Test
    void einKonformerEntwurfWirdVeroeffentlichtUndLoestDieBisherigeVersionAb() {
        var ergebnis = veroeffentlichung.veroeffentliche(VERA, mahnung.id());

        assertThat(ergebnis).isEqualTo(
                new Veroeffentlichungsergebnis.Veroeffentlicht(new Versionsverweis(mahnung.id(), ZWEI)));
        Standardschreiben gespeichert = gespeicherteMahnung();
        assertThat(gespeichert.versionen()).extracting(Vorlagenversion::zustand)
                .containsExactly(Vorlagenzustand.ABGELOEST, Vorlagenzustand.VEROEFFENTLICHT);
        assertThat(gespeichert.version(Versionsnummer.ERSTE)).get().extracting(Vorlagenversion::inhalt)
                .isEqualTo("<p>{{name}}</p>");
        assertThat(host.vorlagenAktionAngefragt).containsExactly(VorlagenAktion.VEROEFFENTLICHEN);
    }

    @Test
    void dasProbePdfEntstehtMitDemVeroeffentlichtenBriefpapierUndDenBeispieldatenBeiderVersionen() {
        List<String> geprueft = new ArrayList<>();
        pruefer.ergebnis = pdf -> {
            geprueft.add(new String(pdf, StandardCharsets.UTF_8));
            return Konformitaet.konform();
        };

        veroeffentlichung.veroeffentliche(VERA, mahnung.id());

        assertThat(renderer.pdfDaten).containsExactly(Datenkontext.aus(Map.of(
                "absender", "Muster GmbH", "betreff", "Mahnung", "name", "Erika Mustermann GmbH")));
        assertThat(geprueft).containsExactly("<header/><p>Erneut: Erika Mustermann GmbH</p>");
    }

    @Test
    void einBriefpapierWirdMitDemEingebautenBeispielinhaltGeprueftUndVeroeffentlicht() {
        vorlagen.ablegen(briefpapier.neuerEntwurf("<header>neu</header>"));
        List<String> geprueft = new ArrayList<>();
        pruefer.ergebnis = pdf -> {
            geprueft.add(new String(pdf, StandardCharsets.UTF_8));
            return Konformitaet.konform();
        };

        var ergebnis = veroeffentlichung.veroeffentliche(VERA, briefpapier.id());

        assertThat(geprueft).containsExactly("<header>neu</header>Beispielinhalt");
        assertThat(renderer.pdfDaten).containsExactly(briefpapier.veroeffentlichteVersion().orElseThrow()
                .beispieldaten());
        assertThat(ergebnis).isEqualTo(
                new Veroeffentlichungsergebnis.Veroeffentlicht(new Versionsverweis(briefpapier.id(), ZWEI)));
        assertThat(vorlagen.briefpapier(briefpapier.id()).orElseThrow().versionen())
                .extracting(Vorlagenversion::zustand)
                .containsExactly(Vorlagenzustand.ABGELOEST, Vorlagenzustand.VEROEFFENTLICHT);
    }

    @Test
    void bestehtDasProbePdfDiePruefungNichtBleibtDieVersionEinEntwurf() {
        pruefer.ergebnis = pdf -> Konformitaet.nichtKonform(List.of("Bild ohne Alternativtext"));

        var ergebnis = veroeffentlichung.veroeffentliche(VERA, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Veroeffentlichungsergebnis.NichtKonform(List.of("Bild ohne Alternativtext")));
        assertThatMahnungUnveraendert();
    }

    @Test
    void laesstSichDiePruefungNichtAusfuehrenWirdNichtVeroeffentlicht() {
        pruefer.ergebnis = pdf -> {
            throw new IllegalStateException("veraPDF nicht erreichbar");
        };

        assertThatIllegalStateException().isThrownBy(() -> veroeffentlichung.veroeffentliche(VERA, mahnung.id()))
                .withMessage("veraPDF nicht erreichbar");
        assertThatMahnungUnveraendert();
    }

    @Test
    void ohneBerechtigungWirdNichtsGeprueftUndNichtsVeroeffentlicht() {
        var ergebnis = veroeffentlichung.veroeffentliche(MAX, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Veroeffentlichungsergebnis.NichtErlaubt());
        assertThat(renderer.pdfs).isZero();
        assertThatMahnungUnveraendert();
    }

    @Test
    void ohneEntwurfGibtEsNichtsZuVeroeffentlichen() {
        vorlagen.ablegen(mahnung.veroeffentlicheEntwurf());
        vorlagen.ablegen(briefpapier.zurueckziehen().neuerEntwurf("<header/>").veroeffentlicheEntwurf());

        assertThat(veroeffentlichung.veroeffentliche(VERA, mahnung.id()))
                .isEqualTo(new Veroeffentlichungsergebnis.KeinEntwurf());
        assertThat(veroeffentlichung.veroeffentliche(VERA, briefpapier.id()))
                .as("ebenso beim Briefpapier").isEqualTo(new Veroeffentlichungsergebnis.KeinEntwurf());
        assertThat(renderer.pdfs).isZero();
    }

    @Test
    void einEntwurfOhneInhaltWirdNichtVeroeffentlicht() {
        vorlagen.ablegen(mahnung.aendereEntwurf("  "));
        vorlagen.ablegen(briefpapier.neuerEntwurf(""));

        assertThat(veroeffentlichung.veroeffentliche(VERA, mahnung.id()))
                .isEqualTo(new Veroeffentlichungsergebnis.EntwurfOhneInhalt());
        assertThat(veroeffentlichung.veroeffentliche(VERA, briefpapier.id()))
                .as("ebenso beim Briefpapier").isEqualTo(new Veroeffentlichungsergebnis.EntwurfOhneInhalt());
        assertThat(renderer.pdfs).isZero();
    }

    @Test
    void ohneVeroeffentlichtesBriefpapierWirdDasStandardschreibenNichtVeroeffentlicht() {
        vorlagen.ablegen(briefpapier.zurueckziehen());

        var ergebnis = veroeffentlichung.veroeffentliche(VERA, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Veroeffentlichungsergebnis.BriefpapierNichtVeroeffentlicht());
        assertThat(renderer.pdfs).isZero();
        assertThatMahnungUnveraendert();
    }

    @Test
    void eineUnbekannteVorlageWirdGemeldet() {
        assertThat(veroeffentlichung.veroeffentliche(VERA, VorlagenId.neu()))
                .isEqualTo(new Veroeffentlichungsergebnis.VorlageUnbekannt());
    }

    @Test
    void hatEinAndererDieVorlageInzwischenGeaendertWirdNichtsVeroeffentlicht() {
        Standardschreiben ungeprueft = mahnung.aendereEntwurf("<p>ungeprüft</p>");
        vorlagen.zuvor = () -> vorlagen.ablegen(ungeprueft);

        var ergebnis = veroeffentlichung.veroeffentliche(VERA, mahnung.id());

        assertThat(ergebnis).isEqualTo(new Veroeffentlichungsergebnis.Veraltet());
        assertThat(gespeicherteMahnung().entwurf()).get().extracting(Vorlagenversion::inhalt)
                .isEqualTo("<p>ungeprüft</p>");
        assertThat(vorlagen.ersetzungen).isZero();
    }

    @Test
    void eineVeroeffentlichteVersionBleibtUnveraendertEineAenderungBeginntEinenNeuenEntwurf() {
        veroeffentlichung.veroeffentliche(VERA, mahnung.id());
        Standardschreiben veroeffentlicht = gespeicherteMahnung();

        assertThatThrownBy(() -> veroeffentlicht.aendereEntwurf("<p>anders</p>"))
                .isInstanceOf(UnzulaessigerZustandswechsel.class);
        Vorlagenversion neuerEntwurf = veroeffentlicht.neuerEntwurf("<p>anders</p>").entwurf().orElseThrow();
        assertThat(neuerEntwurf.nummer()).isEqualTo(new Versionsnummer(3));
        assertThat(neuerEntwurf.beispieldaten()).isEqualTo(mahnung.entwurf().orElseThrow().beispieldaten());
    }

    private Standardschreiben gespeicherteMahnung() {
        return vorlagen.standardschreiben(mahnung.id()).orElseThrow();
    }

    private void assertThatMahnungUnveraendert() {
        assertThat(vorlagen.ersetzungen).isZero();
        assertThat(gespeicherteMahnung().versionen()).extracting(Vorlagenversion::zustand)
                .containsExactly(Vorlagenzustand.VEROEFFENTLICHT, Vorlagenzustand.ENTWURF);
    }
}
