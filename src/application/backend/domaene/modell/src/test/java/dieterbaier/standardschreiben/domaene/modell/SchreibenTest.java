package dieterbaier.standardschreiben.domaene.modell;

import static dieterbaier.standardschreiben.domaene.modell.SnapshotTest.JETZT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SchreibenTest {

    private final Snapshot snapshot = SnapshotTest.snapshot(Datenkontext.aus(Map.of("name", "Erika")));
    private final Dokumentverweis dokument = new Dokumentverweis("ablage/1");
    private final Pruefsumme pdf = Pruefsumme.ueber("%PDF-1.7".getBytes(StandardCharsets.US_ASCII));

    @Test
    void uebernimmtBezugUndVorlagenversionenAusDemSnapshot() {
        SchreibenId id = SchreibenId.neu();
        Instant gespeichert = JETZT.plus(Duration.ofMinutes(5));

        Schreiben schreiben = Schreiben.ausSnapshot(id, snapshot, dokument, pdf, gespeichert);

        assertThat(schreiben).isEqualTo(new Schreiben(id, snapshot.bezug(), snapshot.briefpapier(),
                snapshot.standardschreiben(), dokument, pdf, gespeichert));
    }

    @Test
    void ausEinemAbgelaufenenSnapshotEntstehtKeinSchreiben() {
        assertThatIllegalStateException()
                .isThrownBy(() -> Schreiben.ausSnapshot(SchreibenId.neu(), snapshot, dokument, pdf, snapshot.ablauf()))
                .withMessageContaining("abgelaufen");
    }
}
