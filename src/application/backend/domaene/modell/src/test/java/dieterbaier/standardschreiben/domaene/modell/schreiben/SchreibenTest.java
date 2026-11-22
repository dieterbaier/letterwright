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
    void pflichtattributeDesErgebnisses() {
        Schreiben schreiben = Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, JETZT).schreiben();

        lehntAb(() -> new SchreibenAusSnapshot(null, snapshot), "Das Schreiben");
        lehntAb(() -> new SchreibenAusSnapshot(schreiben, null), "verbrauchte Snapshot");
    }

    @Test
    void dasErgebnisPasstZusammen() {
        SchreibenAusSnapshot erzeugt = Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, JETZT);
        Snapshot andererVerbraucht = Schreiben.ausSnapshot(SchreibenId.neu(), Testdaten.snapshot(), dokument, pdf, JETZT)
                .verbrauchterSnapshot();

        lehntAb(() -> new SchreibenAusSnapshot(erzeugt.schreiben(), snapshot), "nicht verbraucht");
        lehntAb(() -> new SchreibenAusSnapshot(erzeugt.schreiben(), andererVerbraucht), "entstand nicht aus Snapshot");
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
                pdf, JETZT), "Snapshot des Schreibens");
        lehntAb(() -> Schreiben.wiederherstellen(id, snapshot.id(), null, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3, dokument, pdf,
                JETZT), "fachliche Bezug des Schreibens");
        lehntAb(() -> Schreiben.wiederherstellen(id, snapshot.id(), KUNDE, null, STANDARDSCHREIBEN_V3, dokument, pdf, JETZT),
                "Briefpapier-Version");
        lehntAb(() -> Schreiben.wiederherstellen(id, snapshot.id(), KUNDE, BRIEFPAPIER_V1, null, dokument, pdf, JETZT),
                "Standardschreiben-Version");
        lehntAb(() -> Schreiben.wiederherstellen(id, snapshot.id(), KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3, dokument, pdf,
                null), "Erstellungszeitpunkt");
    }

    @Test
    void gleichheitUeberDieIdentitaet() {
        SchreibenId id = SchreibenId.neu();
        Schreiben schreiben = Schreiben.ausSnapshot(id, snapshot, dokument, pdf, JETZT).schreiben();
        Schreiben geladen = Schreiben.wiederherstellen(id, snapshot.id(), KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3,
                new Dokumentverweis("ablage/2"), pdf, JETZT);

        assertThat(geladen).isEqualTo(schreiben).hasSameHashCodeAs(schreiben);
        assertThat(Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, JETZT).schreiben())
                .isNotEqualTo(schreiben);
        assertThat(schreiben).isNotEqualTo(id);
        assertThat(schreiben.toString()).contains(id.wert().toString());
    }
}
