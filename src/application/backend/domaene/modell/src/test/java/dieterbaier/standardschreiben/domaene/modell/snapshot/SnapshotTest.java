package dieterbaier.standardschreiben.domaene.modell.snapshot;

import static dieterbaier.standardschreiben.domaene.modell.Pruefungen.lehntAb;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.BRIEFPAPIER_V1;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.DATENKONTEXT;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.JETZT;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.KUNDE;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.SACHBEARBEITER;
import static dieterbaier.standardschreiben.domaene.modell.Testdaten.STANDARDSCHREIBEN_V3;
import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Testdaten;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SnapshotTest {

    private final Snapshot snapshot = Testdaten.snapshot();

    @Test
    void laeuftDreissigMinutenNachDerAnlageAb() {
        assertThat(snapshot.ablauf()).isEqualTo(JETZT.plus(Duration.ofMinutes(30)));
        assertThat(snapshot.istAbgelaufen(snapshot.ablauf().minusMillis(1))).isFalse();
        assertThat(snapshot.istAbgelaufen(snapshot.ablauf())).isTrue();
    }

    @Test
    void bindetBenutzerBezugUndBeideVorlagenversionen() {
        assertThat(snapshot.benutzer()).isEqualTo(SACHBEARBEITER);
        assertThat(snapshot.bezug()).isEqualTo(KUNDE);
        assertThat(snapshot.briefpapier()).isEqualTo(BRIEFPAPIER_V1);
        assertThat(snapshot.standardschreiben()).isEqualTo(STANDARDSCHREIBEN_V3);
        assertThat(snapshot.zustand()).isEqualTo(new Snapshotzustand.Offen(DATENKONTEXT));
    }

    @Test
    void istFuerDenselbenBenutzerUndBezugVerwendbar() {
        assertThat(snapshot.istVerwendbarFuer(SACHBEARBEITER, KUNDE, JETZT)).isTrue();
    }

    @Test
    void einFremderBenutzerKannIhnNichtVerwenden() {
        assertThat(snapshot.istVerwendbarFuer(new BenutzerKennung("max"), KUNDE, JETZT)).isFalse();
    }

    @Test
    void fuerEinenAnderenBezugIstErNichtVerwendbar() {
        assertThat(snapshot.istVerwendbarFuer(SACHBEARBEITER, new FachlicherBezug("Kunde", "4712"), JETZT))
                .isFalse();
    }

    @Test
    void nachAblaufIstErNichtVerwendbar() {
        assertThat(snapshot.istVerwendbarFuer(SACHBEARBEITER, KUNDE, snapshot.ablauf())).isFalse();
    }

    @Test
    void einVerbrauchterSnapshotHaeltKeinenDatenkontextMehr() {
        Snapshot verbraucht = snapshot.verbraucht();

        assertThat(verbraucht.zustand()).isEqualTo(new Snapshotzustand.Verbraucht());
        assertThat(verbraucht).isEqualTo(snapshot);
        assertThat(verbraucht.benutzer()).isEqualTo(SACHBEARBEITER);
        assertThat(verbraucht.bezug()).isEqualTo(KUNDE);
        assertThat(verbraucht.standardschreiben()).isEqualTo(STANDARDSCHREIBEN_V3);
        assertThat(verbraucht.ablauf()).isEqualTo(snapshot.ablauf());
    }

    @Test
    void einSnapshotWirdNurEinmalVerbraucht() {
        Snapshot verbraucht = snapshot.verbraucht();

        org.assertj.core.api.Assertions.assertThatIllegalStateException().isThrownBy(verbraucht::verbraucht)
                .withMessageContaining("schon ein Schreiben");
    }

    @Test
    void pflichtattributeBeimAnlegen() {
        SnapshotId id = SnapshotId.neu();

        lehntAb(() -> Snapshot.anlegen(null, SACHBEARBEITER, KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3,
                DATENKONTEXT, JETZT), "ID des Snapshots");
        lehntAb(() -> Snapshot.anlegen(id, null, KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3, DATENKONTEXT, JETZT),
                "Benutzer des Snapshots");
        lehntAb(() -> Snapshot.anlegen(id, SACHBEARBEITER, null, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3, DATENKONTEXT,
                JETZT), "fachliche Bezug des Snapshots");
        lehntAb(() -> Snapshot.anlegen(id, SACHBEARBEITER, KUNDE, null, STANDARDSCHREIBEN_V3, DATENKONTEXT, JETZT),
                "Briefpapier-Version");
        lehntAb(() -> Snapshot.anlegen(id, SACHBEARBEITER, KUNDE, BRIEFPAPIER_V1, null, DATENKONTEXT, JETZT),
                "Standardschreiben-Version");
        lehntAb(() -> Snapshot.anlegen(id, SACHBEARBEITER, KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3, null, JETZT),
                "Datenkontext des Snapshots");
        lehntAb(() -> Snapshot.anlegen(id, SACHBEARBEITER, KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3, DATENKONTEXT,
                null), "Zeitpunkt der Anlage");
        lehntAb(() -> Snapshot.wiederherstellen(id, SACHBEARBEITER, KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3,
                new Snapshotzustand.Offen(DATENKONTEXT), null), "Ablaufzeitpunkt");
        lehntAb(() -> Snapshot.wiederherstellen(id, SACHBEARBEITER, KUNDE, BRIEFPAPIER_V1, STANDARDSCHREIBEN_V3, null,
                JETZT), "Zustand des Snapshots");
    }

    @Test
    void wiederherstellenBehaeltDenAblauf() {
        Instant ablauf = JETZT.plusSeconds(5);

        Snapshot geladen = Snapshot.wiederherstellen(snapshot.id(), SACHBEARBEITER, KUNDE, BRIEFPAPIER_V1,
                STANDARDSCHREIBEN_V3, new Snapshotzustand.Verbraucht(), ablauf);

        assertThat(geladen.ablauf()).isEqualTo(ablauf);
        assertThat(geladen).as("gleich über die ID").isEqualTo(snapshot).hasSameHashCodeAs(snapshot);
    }

    @Test
    void gleichheitUeberDieIdentitaetUndToStringOhneDaten() {
        assertThat(Testdaten.snapshot()).isNotEqualTo(snapshot);
        assertThat(snapshot).isNotEqualTo(snapshot.id());
        assertThat(snapshot.toString()).contains(snapshot.id().wert().toString()).doesNotContain("Erika");
    }
}
