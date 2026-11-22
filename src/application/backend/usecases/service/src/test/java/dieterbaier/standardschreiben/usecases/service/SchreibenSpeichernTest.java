package dieterbaier.standardschreiben.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Briefpapier;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import dieterbaier.standardschreiben.usecases.in.Speicherergebnis;
import dieterbaier.standardschreiben.usecases.in.Vorschauergebnis;
import dieterbaier.standardschreiben.usecases.out.Konformitaet;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Brücke zu {@code features/schreiben/schreiben-speichern.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die Vorschau entsteht mit dem echten Anwendungsfall, damit Snapshot und Speichern zusammenpassen; die
 * ausgehenden Ports sind {@link Attrappen}.
 */
class SchreibenSpeichernTest {

    private static final Instant VORSCHAU = Instant.parse("2026-11-22T10:00:00Z");
    private static final BenutzerKennung ERIKA = new BenutzerKennung("erika");
    private static final BenutzerKennung MAX = new BenutzerKennung("max");
    private static final FachlicherBezug KUNDE_4711 = new FachlicherBezug("Kunde", "4711");

    private final Attrappen.Vorlagen vorlagen = new Attrappen.Vorlagen();
    private final Attrappen.Host host = new Attrappen.Host();
    private final Attrappen.Snapshots snapshots = new Attrappen.Snapshots();
    private final Attrappen.Renderer renderer = new Attrappen.Renderer();
    private final Attrappen.Pruefer pruefer = new Attrappen.Pruefer();
    private final Attrappen.Dokumente dokumente = new Attrappen.Dokumente();
    private final Attrappen.SchreibenAblage schreiben = new Attrappen.SchreibenAblage();
    private final Attrappen.Uhr uhr = new Attrappen.Uhr(VORSCHAU);
    private final Vorschauerzeugung vorschau = new Vorschauerzeugung(vorlagen, host, host, renderer, snapshots, uhr);
    private final Schreibenspeicherung speicherung = new Schreibenspeicherung(snapshots, vorlagen, host, host, renderer,
            pruefer, dokumente, schreiben, uhr);

    private final Briefpapier briefpapier = vorlagen.ablegen(
            Briefpapier.anlegenMitStandardtyp(VorlagenId.neu(), "Standard", "<header/>").veroeffentlicheEntwurf());
    private final Standardschreiben mahnung = vorlagen.ablegen(Standardschreiben.anlegen(VorlagenId.neu(), "Mahnung",
            briefpapier.id(), "Kunde", "<p>{{name}}</p>").veroeffentlicheEntwurf());

    // Grundlage: Erika hat vor 10 Minuten eine Vorschau erzeugt, darf erzeugen, der Host liefert dieselben Daten.
    private final SnapshotId vorschauVonErika = erzeugeVorschau(ERIKA);

    {
        uhr.jetzt = VORSCHAU.plus(Duration.ofMinutes(10));
        host.erzeugenAngefragt.clear();
        host.datenkontextAngefragt = 0;
    }

    @Test
    void ausEinerUnveraendertenVorschauWirdDasGeprueftePdfGespeichert() {
        var ergebnis = speicherung.speichere(ERIKA, vorschauVonErika);

        Schreiben gespeichert = schreiben.gespeichert.get(vorschauVonErika);
        assertThat(ergebnis).isEqualTo(new Speicherergebnis.Gespeichert(gespeichert.id()));
        Snapshot snapshot = snapshots.lade(vorschauVonErika).orElseThrow();
        assertThat(gespeichert.briefpapier()).isEqualTo(snapshot.briefpapier());
        assertThat(gespeichert.standardschreiben()).isEqualTo(snapshot.standardschreiben());
        assertThat(gespeichert.bezug()).isEqualTo(KUNDE_4711);
        assertThat(gespeichert.erstellt()).isEqualTo(uhr.jetzt);
        byte[] pdf = dokumente.abgelegt.get(gespeichert.dokument());
        assertThat(new String(pdf, StandardCharsets.UTF_8)).isEqualTo("<header/><p>Erika Mustermann GmbH</p>");
        assertThat(gespeichert.pdfPruefsumme()).isEqualTo(Pruefsumme.ueber(pdf));
    }

    @Test
    void wirdDieBerechtigungBeimSpeichernEntzogenWirdNichtsPersistiert() {
        host.erlaubt = false;

        var ergebnis = speicherung.speichere(ERIKA, vorschauVonErika);

        assertThat(ergebnis).isInstanceOf(Speicherergebnis.NichtErlaubt.class);
        assertThat(host.erzeugenAngefragt).containsExactly(mahnung);
        assertThat(renderer.pdfs).isZero();
        nichtsGespeichertOderAbgelegt();
    }

    @Test
    void einNichtKonformesPdfWirdNichtGespeichert() {
        pruefer.ergebnis = pdf -> Konformitaet.nichtKonform(List.of("PDF/UA-1: Bild ohne Alternativtext"));

        var ergebnis = speicherung.speichere(ERIKA, vorschauVonErika);

        assertThat(ergebnis)
                .isEqualTo(new Speicherergebnis.NichtKonform(List.of("PDF/UA-1: Bild ohne Alternativtext")));
        nichtsGespeichertOderAbgelegt();
    }

    @Test
    void einNichtPruefbaresPdfWirdNichtGespeichert() {
        pruefer.ergebnis = pdf -> {
            throw new IllegalStateException("veraPDF nicht erreichbar");
        };

        assertThatThrownBy(() -> speicherung.speichere(ERIKA, vorschauVonErika))
                .hasMessageContaining("veraPDF nicht erreichbar");
        nichtsGespeichertOderAbgelegt();
    }

    @Test
    void nachEinerAenderungDesDatenkontextsWirdKeinPdfErzeugt() {
        host.datenkontext = Datenkontext.aus(Map.of("name", "Erika Musterfrau AG"));

        var ergebnis = speicherung.speichere(ERIKA, vorschauVonErika);

        assertThat(ergebnis).isInstanceOf(Speicherergebnis.DatenkontextGeaendert.class);
        assertThat(renderer.pdfs).isZero();
        nichtsGespeichertOderAbgelegt();
    }

    @Test
    void nachEinerNeuenVorschauGelingtDasSpeichern() {
        host.datenkontext = Datenkontext.aus(Map.of("name", "Erika Musterfrau AG"));
        var aktualisiert = (Vorschauergebnis.Erzeugt) vorschau.aktualisiere(ERIKA, KUNDE_4711, mahnung.id(),
                vorschauVonErika);

        var ergebnis = speicherung.speichere(ERIKA, aktualisiert.snapshot());

        assertThat(ergebnis).isInstanceOf(Speicherergebnis.Gespeichert.class);
        Schreiben gespeichert = schreiben.gespeichert.get(aktualisiert.snapshot());
        assertThat(new String(dokumente.abgelegt.get(gespeichert.dokument()), StandardCharsets.UTF_8))
                .contains("Erika Musterfrau AG");
    }

    @Test
    void einFremderSnapshotVerhaeltSichWieEinNichtVorhandener() {
        assertThat(speicherung.speichere(MAX, vorschauVonErika)).isInstanceOf(Speicherergebnis.SnapshotUnbekannt.class);
        assertThat(speicherung.speichere(ERIKA, SnapshotId.neu())).isInstanceOf(Speicherergebnis.SnapshotUnbekannt.class);
        assertThat(host.wurdeGefragt()).isFalse();
    }

    @Test
    void einAbgelaufenerSnapshotWirdNichtGespeichert() {
        uhr.jetzt = VORSCHAU.plus(Duration.ofMinutes(30));

        assertThat(speicherung.speichere(ERIKA, vorschauVonErika))
                .isInstanceOf(Speicherergebnis.SnapshotAbgelaufen.class);
        assertThat(host.wurdeGefragt()).isFalse();
        nichtsGespeichertOderAbgelegt();
    }

    @Test
    void wiederholtesSpeichernDerselbenVorschauErgibtKeinZweitesSchreiben() {
        var erstes = (Speicherergebnis.Gespeichert) speicherung.speichere(ERIKA, vorschauVonErika);

        var erneut = speicherung.speichere(ERIKA, vorschauVonErika);

        assertThat(erneut).isEqualTo(new Speicherergebnis.BereitsGespeichert(erstes.schreiben()));
        assertThat(renderer.pdfs).isEqualTo(1);
        assertThat(dokumente.abgelegt).hasSize(1);
    }

    @Test
    void gleichzeitigesSpeichernDerselbenVorschauErgibtEinSchreiben() {
        Snapshot snapshot = snapshots.lade(vorschauVonErika).orElseThrow();
        Schreiben derZweitenAnfrage = Schreiben.ausSnapshot(SchreibenId.neu(), snapshot,
                new Dokumentverweis("ablage/zweite"), Pruefsumme.ueber(new byte[] {1}), uhr.jetzt);
        schreiben.zuvor = Optional.of(derZweitenAnfrage);

        var ergebnis = speicherung.speichere(ERIKA, vorschauVonErika);

        assertThat(ergebnis).isEqualTo(new Speicherergebnis.BereitsGespeichert(derZweitenAnfrage.id()));
        assertThat(schreiben.gespeichert).containsOnly(Map.entry(vorschauVonErika, derZweitenAnfrage));
        assertThat(dokumente.abgelegt).isEmpty();
    }

    @Test
    void eineNeueVorschauErgibtEinNeuesSchreiben() {
        var erstes = (Speicherergebnis.Gespeichert) speicherung.speichere(ERIKA, vorschauVonErika);
        SnapshotId neueVorschau = erzeugeVorschau(ERIKA);

        var zweites = speicherung.speichere(ERIKA, neueVorschau);

        assertThat(zweites).isInstanceOf(Speicherergebnis.Gespeichert.class).isNotEqualTo(erstes);
        assertThat(schreiben.gespeichert).hasSize(2);
    }

    @Test
    void ohneBerechtigungBleibtVerborgenObSchonGespeichertWurde() {
        speicherung.speichere(ERIKA, vorschauVonErika);
        host.erlaubt = false;

        assertThat(speicherung.speichere(ERIKA, vorschauVonErika)).isInstanceOf(Speicherergebnis.NichtErlaubt.class);
    }

    @Test
    void scheitertDasSpeichernWirdDasAbgelegtePdfEntfernt() {
        schreiben.fehler = Optional.of(new IllegalStateException("Datenbank nicht erreichbar"));

        assertThatThrownBy(() -> speicherung.speichere(ERIKA, vorschauVonErika))
                .hasMessageContaining("Datenbank nicht erreichbar");
        nichtsGespeichertOderAbgelegt();
    }

    @Test
    void eineFehlendeVorlagenversionIstEinDefekterBestand() {
        vorlagen.briefpapiere.clear();

        assertThatThrownBy(() -> speicherung.speichere(ERIKA, vorschauVonErika))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("fehlt");
        vorlagen.standardschreiben.clear();
        assertThatThrownBy(() -> speicherung.speichere(ERIKA, vorschauVonErika))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("fehlt");
    }

    private SnapshotId erzeugeVorschau(BenutzerKennung benutzer) {
        host.erlaubt = true;
        return ((Vorschauergebnis.Erzeugt) vorschau.erzeuge(benutzer, KUNDE_4711, mahnung.id())).snapshot();
    }

    private void nichtsGespeichertOderAbgelegt() {
        assertThat(schreiben.gespeichert).isEmpty();
        assertThat(dokumente.abgelegt).isEmpty();
    }
}
