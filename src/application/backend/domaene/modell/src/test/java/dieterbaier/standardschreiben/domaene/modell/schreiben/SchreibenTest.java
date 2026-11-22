package dieterbaier.standardschreiben.domaene.modell.schreiben;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.BRIEFPAPIER_V1;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.JETZT;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.KUNDE;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.STANDARDSCHREIBEN_V3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.Testdaten;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshotzustand;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SchreibenTest {

    private final Snapshot snapshot = Testdaten.snapshot();
    private final Dokumentverweis dokument = new Dokumentverweis("ablage/1");
    private final Pruefsumme pdf = Pruefsumme.ueber("%PDF-1.7".getBytes(StandardCharsets.US_ASCII));
    private static final Schreibenzustand SICHTBAR = new Schreibenzustand.Sichtbar();

    @Test
    void uebernimmtBezugUndVorlagenversionenAusDemSnapshot() {
        SchreibenId id = SchreibenId.neu();
        Instant gespeichert = JETZT.plus(Duration.ofMinutes(5));

        SchreibenAusSnapshot erzeugt = Schreiben.ausSnapshot(id, snapshot, dokument, pdf, gespeichert);
        Schreiben schreiben = erzeugt.schreiben();

        assertThat(schreiben.id()).isEqualTo(id);
        assertThat(schreiben.snapshot()).isEqualTo(snapshot.id());
        assertThat(schreiben.bezug()).isEqualTo(KUNDE);
        assertThat(schreiben.briefpapier()).isEqualTo(BRIEFPAPIER_V1);
        assertThat(schreiben.standardschreiben()).isEqualTo(STANDARDSCHREIBEN_V3);
        assertThat(schreiben.dokument()).isEqualTo(dokument);
        assertThat(schreiben.pdfPruefsumme()).isEqualTo(pdf);
        assertThat(schreiben.erstellt()).isEqualTo(gespeichert);
        assertThat(schreiben.istSichtbar()).as("ein neues Schreiben ist sichtbar").isTrue();
        assertThat(erzeugt.verbrauchterSnapshot()).isEqualTo(snapshot);
        assertThat(erzeugt.verbrauchterSnapshot().zustand()).isInstanceOf(Snapshotzustand.Verbraucht.class);
    }

    @Test
    void ausEinemVerbrauchtenSnapshotEntstehtKeinWeiteresSchreiben() {
        Snapshot verbraucht = Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, JETZT)
                .verbrauchterSnapshot();

        assertThatIllegalStateException()
                .isThrownBy(() -> Schreiben.ausSnapshot(SchreibenId.neu(), verbraucht, dokument, pdf, JETZT))
                .withMessageContaining("schon ein Schreiben");
    }

    @Test
    void ausEinemAbgelaufenenSnapshotEntstehtKeinSchreiben() {
        assertThatIllegalStateException()
                .isThrownBy(() -> Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, snapshot.ablauf()))
                .withMessageContaining("abgelaufen");
    }

    @Test
    void pflichtattribute() {
        SchreibenId id = SchreibenId.neu();

        lehntAb(() -> Schreiben.ausSnapshot(id, null, dokument, pdf, JETZT), "Snapshot");
        lehntAb(() -> Schreiben.ausSnapshot(id, snapshot, dokument, pdf, null), "Zeitpunkt");
        lehntAb(() -> Schreiben.ausSnapshot(null, snapshot, dokument, pdf, JETZT), "ID des Schreibens");
        lehntAb(() -> Schreiben.ausSnapshot(id, snapshot, null, pdf, JETZT), "Dokumentverweis des Schreibens");
        lehntAb(() -> Schreiben.ausSnapshot(id, snapshot, dokument, null, JETZT), "Prüfsumme des PDFs");
        lehntAb(() -> Schreiben.wiederherstellen(id, null, KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3, dokument,
                pdf, JETZT, SICHTBAR), "Snapshot des Schreibens");
        lehntAb(() -> Schreiben.wiederherstellen(id, snapshot.id(), null, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3,
                dokument, pdf, JETZT, SICHTBAR), "fachliche Bezug des Schreibens");
        lehntAb(() -> Schreiben.wiederherstellen(id, snapshot.id(), KUNDE, null, STANDARDSCHREIBEN_V3, dokument, pdf,
                JETZT, SICHTBAR), "Briefpapier-Version");
        lehntAb(() -> Schreiben.wiederherstellen(id, snapshot.id(), KUNDE, BRIEFPAPIER_V1, null, dokument, pdf, JETZT,
                SICHTBAR), "Standardschreiben-Version");
        lehntAb(() -> Schreiben.wiederherstellen(id, snapshot.id(), KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3,
                dokument, pdf, null, SICHTBAR), "Erstellungszeitpunkt");
        lehntAb(() -> Schreiben.wiederherstellen(id, snapshot.id(), KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3,
                dokument, pdf, JETZT, null), "Zustand des Schreibens");
        lehntAb(() -> new Schreibenzustand.LogischGeloescht(null), "Zeitpunkt des Löschens");
    }

    @Test
    void gleichheitUeberDieIdentitaet() {
        SchreibenId id = SchreibenId.neu();
        Schreiben schreiben = Schreiben.ausSnapshot(id, snapshot, dokument, pdf, JETZT).schreiben();
        Schreiben geladen = Schreiben.wiederherstellen(id, snapshot.id(), KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3,
                new Dokumentverweis("ablage/2"), pdf, JETZT, SICHTBAR);

        assertThat(geladen).isEqualTo(schreiben).hasSameHashCodeAs(schreiben);
        assertThat(Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, JETZT).schreiben())
                .isNotEqualTo(schreiben);
        assertThat(schreiben).isNotEqualTo(id);
        assertThat(schreiben.toString()).contains(id.wert().toString());
    }

    @Test
    void einLogischGeloeschtesSchreibenBehaeltAllesAusserDerSichtbarkeit() {
        Schreiben schreiben = Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, JETZT).schreiben();
        Instant geloescht = JETZT.plus(Duration.ofDays(1));

        Schreiben ergebnis = schreiben.logischGeloescht(geloescht);

        assertThat(ergebnis.istSichtbar()).isFalse();
        assertThat(ergebnis.zustand()).isEqualTo(new Schreibenzustand.LogischGeloescht(geloescht));
        assertThat(ergebnis).isEqualTo(schreiben);
        assertThat(ergebnis.dokument()).isEqualTo(dokument);
        assertThat(ergebnis.pdfPruefsumme()).isEqualTo(pdf);
        assertThat(ergebnis.erstellt()).isEqualTo(JETZT);
        assertThat(schreiben.istSichtbar()).as("das Original bleibt unverändert").isTrue();
    }

    @Test
    void einLogischGeloeschtesSchreibenWirdNichtErneutGeloescht() {
        Schreiben geloescht = Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, JETZT).schreiben()
                .logischGeloescht(JETZT);

        assertThatIllegalStateException().isThrownBy(() -> geloescht.logischGeloescht(JETZT))
                .withMessageContaining("schon logisch gelöscht");
        lehntAb(() -> Schreiben.ausSnapshot(SchreibenId.neu(), Testdaten.snapshot(), dokument, pdf, JETZT).schreiben()
                .logischGeloescht(null), "Zeitpunkt");
    }
}
