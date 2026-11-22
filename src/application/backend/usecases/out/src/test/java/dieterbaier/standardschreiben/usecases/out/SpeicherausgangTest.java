package dieterbaier.standardschreiben.usecases.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SpeicherausgangTest {

    private final Schreiben vorhandenes = Schreiben.wiederherstellen(SchreibenId.neu(), SnapshotId.neu(),
            new FachlicherBezug("Kunde", "4711"), new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE),
            new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE), new Dokumentverweis("ablage/1"),
            Pruefsumme.ueber(new byte[] {1}), Instant.parse("2026-11-22T10:00:00Z"));

    @Test
    void einVerbrauchterSnapshotNenntDasVorhandeneSchreiben() {
        assertThat(new Speicherausgang.SnapshotBereitsVerbraucht(vorhandenes).vorhandenes()).isEqualTo(vorhandenes);
        assertThat(new Speicherausgang.Gespeichert()).isInstanceOf(Speicherausgang.class);
        assertThatThrownBy(() -> new Speicherausgang.SnapshotBereitsVerbraucht(null))
                .hasMessageContaining("vorhandene Schreiben");
    }
}
