package dieterbaier.standardschreiben.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshotzustand;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Briefpapier;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import dieterbaier.standardschreiben.usecases.in.Vorschauergebnis;
import dieterbaier.standardschreiben.usecases.out.Renderer;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Brücke zu {@code features/schreiben/vorschau-erzeugen.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die ausgehenden Ports sind {@link Attrappen}, die festhalten, was gefragt wurde.
 */
class VorschauerzeugungTest {

    private static final Instant JETZT = Instant.parse("2026-11-22T10:00:00Z");
    private static final BenutzerKennung ERIKA = new BenutzerKennung("erika");
    private static final BenutzerKennung MAX = new BenutzerKennung("max");
    private static final FachlicherBezug KUNDE_4711 = new FachlicherBezug("Kunde", "4711");

    private final Attrappen.Vorlagen vorlagen = new Attrappen.Vorlagen();
    private final Attrappen.Host host = new Attrappen.Host();
    private final Attrappen.Snapshots ablage = new Attrappen.Snapshots();
    private final Attrappen.Uhr uhr = new Attrappen.Uhr(JETZT);
    private final Attrappen.Renderer renderer = new Attrappen.Renderer();
    private final Vorschauerzeugung vorschau = new Vorschauerzeugung(vorlagen, host, host, renderer, ablage, uhr);

    // Grundlage: "Mahnung" zum Bezugstyp Kunde, veröffentlicht bis Version 2, Entwurf 3; Briefpapier in Version 1;
    // der Host liefert zum Kunden 4711 den Datenkontext "name" = "Erika Mustermann GmbH".
    private final Briefpapier briefpapier =
            vorlagen.ablegen(Briefpapier.anlegenMitStandardtyp(VorlagenId.neu(), "Standard", "<header/>")
                    .veroeffentlicheEntwurf());
    private final Standardschreiben mahnung = vorlagen.ablegen(Standardschreiben.anlegen(VorlagenId.neu(), "Mahnung",
            briefpapier.id(), "Kunde", "<p>{{name}}</p>").veroeffentlicheEntwurf()
            .neuerEntwurf("<p>Erneut: {{name}}</p>").veroeffentlicheEntwurf().neuerEntwurf("<p>Entwurf</p>"));

    @Test
    void mitBerechtigungEntstehtEineVorschauMitSnapshot() {
        // Given: Erika darf zum Kunden 4711 aus der Mahnung ein Schreiben erzeugen
        host.erlaubt = true;

        // When: Erika erzeugt die Vorschau der Mahnung zum Kunden 4711
        var ergebnis = vorschau.erzeuge(ERIKA, KUNDE_4711, mahnung.id());

        // Then: Vorschau mit den Daten; Snapshot gebunden an Erika, Kunde, Briefpapier V1 und Mahnung V2; 30 Minuten
        var erzeugt = (Vorschauergebnis.Erzeugt) ergebnis;
        assertThat(erzeugt.html()).contains("Erneut: Erika Mustermann GmbH").contains("<header/>");
        Snapshot snapshot = ablage.snapshots.get(erzeugt.snapshot());
        assertThat(snapshot.benutzer()).isEqualTo(ERIKA);
        assertThat(snapshot.bezug()).isEqualTo(KUNDE_4711);
        assertThat(snapshot.briefpapier()).isEqualTo(new Versionsverweis(briefpapier.id(), Versionsnummer.ERSTE));
        assertThat(snapshot.standardschreiben()).isEqualTo(new Versionsverweis(mahnung.id(), new Versionsnummer(2)));
        assertThat(snapshot.zustand()).isEqualTo(new Snapshotzustand.Offen(host.datenkontext));
        assertThat(erzeugt.ablauf()).isEqualTo(snapshot.ablauf()).isEqualTo(JETZT.plus(Duration.ofMinutes(30)));
        assertThat(host.erzeugenAngefragt).containsExactly(mahnung);
    }

    @Test
    void ohneBerechtigungWirdKeinDatenkontextAngefragt() {
        // Given: Erika darf aus der Mahnung kein Schreiben erzeugen
        host.erlaubt = false;

        // When
        var ergebnis = vorschau.erzeuge(ERIKA, KUNDE_4711, mahnung.id());

        // Then
        assertThat(ergebnis).isInstanceOf(Vorschauergebnis.NichtErlaubt.class);
        assertThat(host.erzeugenAngefragt).containsExactly(mahnung);
        assertThat(host.datenkontextAngefragt).isZero();
        assertThat(renderer.aufrufe).isZero();
        assertThat(ablage.snapshots).isEmpty();
    }

    @Test
    void zuEinemBezugAnderenTypsWirdDerHostNichtGefragt() {
        host.erlaubt = true;

        var ergebnis = vorschau.erzeuge(ERIKA, new FachlicherBezug("Lieferant", "0815"), mahnung.id());

        assertThat(ergebnis).isInstanceOf(Vorschauergebnis.BezugPasstNicht.class);
        assertThat(host.wurdeGefragt()).isFalse();
        assertThat(ablage.snapshots).isEmpty();
    }

    @Test
    void zuEinemUnbekanntenStandardschreibenGibtEsKeineVorschau() {
        host.erlaubt = true;

        var ergebnis = vorschau.erzeuge(ERIKA, KUNDE_4711, VorlagenId.neu());

        assertThat(ergebnis).isInstanceOf(Vorschauergebnis.StandardschreibenUnbekannt.class);
        assertThat(host.wurdeGefragt()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"das Standardschreiben", "das Briefpapier"})
    void ohneVeroeffentlichteVersionGibtEsKeineVorschau(String vorlage) {
        // Given: <vorlage> hat keine veröffentlichte Version; Erika darf erzeugen
        ohneVeroeffentlichteVersion(vorlage);
        host.erlaubt = true;

        var ergebnis = vorschau.erzeuge(ERIKA, KUNDE_4711, mahnung.id());

        assertThat(ergebnis).isInstanceOf(Vorschauergebnis.KeineVeroeffentlichteVersion.class);
        assertThat(host.erzeugenAngefragt).containsExactly(mahnung);
        assertThat(host.datenkontextAngefragt).isZero();
        assertThat(ablage.snapshots).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"das Standardschreiben", "das Briefpapier"})
    void ohneBerechtigungBleibtVerborgenObEineVersionVeroeffentlichtIst(String vorlage) {
        // Given: <vorlage> hat keine veröffentlichte Version; Erika darf nicht erzeugen
        ohneVeroeffentlichteVersion(vorlage);
        host.erlaubt = false;

        var ergebnis = vorschau.erzeuge(ERIKA, KUNDE_4711, mahnung.id());

        // Then: nicht erlaubt, wie mit veröffentlichter Version; kein Datenkontext, kein Rendering, kein Snapshot
        assertThat(ergebnis).isInstanceOf(Vorschauergebnis.NichtErlaubt.class);
        assertThat(host.datenkontextAngefragt).isZero();
        assertThat(renderer.aufrufe).isZero();
        assertThat(ablage.snapshots).isEmpty();
    }

    private void ohneVeroeffentlichteVersion(String vorlage) {
        if (vorlage.equals("das Standardschreiben")) {
            vorlagen.ablegen(mahnung.zurueckziehen(Versionsnummer.ERSTE).zurueckziehen(new Versionsnummer(2)));
        } else {
            vorlagen.ablegen(briefpapier.zurueckziehen(Versionsnummer.ERSTE));
        }
    }

    @Test
    void einFehlendesBriefpapierZaehltWieEinesOhneVeroeffentlichteVersion() {
        vorlagen.briefpapiere.clear();
        host.erlaubt = true;

        assertThat(vorschau.erzeuge(ERIKA, KUNDE_4711, mahnung.id()))
                .isInstanceOf(Vorschauergebnis.KeineVeroeffentlichteVersion.class);
    }

    @Test
    void eineAktualisierteVorschauErsetztDenEigenenSnapshot() {
        // Given: Erika darf erzeugen und hat vor 20 Minuten eine Vorschau erzeugt
        host.erlaubt = true;
        var bisherige = (Vorschauergebnis.Erzeugt) vorschau.erzeuge(ERIKA, KUNDE_4711, mahnung.id());
        uhr.jetzt = JETZT.plus(Duration.ofMinutes(20));

        // When: Erika aktualisiert diese Vorschau
        var neue = (Vorschauergebnis.Erzeugt) vorschau.aktualisiere(ERIKA, KUNDE_4711, mahnung.id(),
                bisherige.snapshot());

        // Then: Der bisherige Snapshot ist gelöscht, der neue läuft in 30 Minuten ab
        assertThat(ablage.snapshots).containsOnlyKeys(neue.snapshot());
        assertThat(neue.ablauf()).isEqualTo(uhr.jetzt.plus(Duration.ofMinutes(30)));
    }

    @Test
    void einFremderSnapshotVerhaeltSichWieEinNichtVorhandener() {
        // Given: Erika darf erzeugen; Max hat eine Vorschau der Mahnung zum Kunden 4711
        host.erlaubt = true;
        var vonMax = (Vorschauergebnis.Erzeugt) vorschau.erzeuge(MAX, KUNDE_4711, mahnung.id());
        Snapshot snapshotVonMax = ablage.snapshots.get(vonMax.snapshot());

        // When: Erika aktualisiert und gibt dabei den Snapshot von Max an
        var ergebnis = vorschau.aktualisiere(ERIKA, KUNDE_4711, mahnung.id(), vonMax.snapshot());

        // Then: Für Erika entsteht eine neue Vorschau; der Snapshot von Max ist unverändert gespeichert
        var fuerErika = (Vorschauergebnis.Erzeugt) ergebnis;
        assertThat(ablage.snapshots.get(fuerErika.snapshot()).benutzer()).isEqualTo(ERIKA);
        assertThat(ablage.snapshots.get(vonMax.snapshot())).isSameAs(snapshotVonMax);
        assertThat(ablage.geloescht).isEmpty();
    }

    @Test
    void eineAktualisierteVorschauLaesstEinenVerbrauchtenSnapshotStehen() {
        // Given: Erika hat aus ihrer bisherigen Vorschau schon ein Schreiben gespeichert
        host.erlaubt = true;
        var bisherige = (Vorschauergebnis.Erzeugt) vorschau.erzeuge(ERIKA, KUNDE_4711, mahnung.id());
        ablage.speichere(ablage.snapshots.get(bisherige.snapshot()).verbraucht());

        // When: Erika aktualisiert diese Vorschau
        var neue = (Vorschauergebnis.Erzeugt) vorschau.aktualisiere(ERIKA, KUNDE_4711, mahnung.id(),
                bisherige.snapshot());

        // Then: Der verbrauchte Snapshot bleibt, damit eine Wiederholung des Speicherns das Schreiben noch findet
        assertThat(ablage.snapshots).containsOnlyKeys(bisherige.snapshot(), neue.snapshot());
        assertThat(ablage.geloescht).isEmpty();
    }

    @Test
    void einUnbekannterBisherigerSnapshotWirdIgnoriert() {
        host.erlaubt = true;

        var ergebnis = vorschau.aktualisiere(ERIKA, KUNDE_4711, mahnung.id(), SnapshotId.neu());

        assertThat(ergebnis).isInstanceOf(Vorschauergebnis.Erzeugt.class);
        assertThat(ablage.geloescht).isEmpty();
    }

    @Test
    void ohneNeueVorschauBleibtDerBisherigeSnapshot() {
        host.erlaubt = true;
        var bisherige = (Vorschauergebnis.Erzeugt) vorschau.erzeuge(ERIKA, KUNDE_4711, mahnung.id());
        host.erlaubt = false;

        var ergebnis = vorschau.aktualisiere(ERIKA, KUNDE_4711, mahnung.id(), bisherige.snapshot());

        assertThat(ergebnis).isInstanceOf(Vorschauergebnis.NichtErlaubt.class);
        assertThat(ablage.snapshots).containsOnlyKeys(bisherige.snapshot());
    }
}
