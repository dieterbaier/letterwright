package dieterbaier.standardschreiben.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class VorschauergebnisTest {

    private final SnapshotId snapshot = SnapshotId.neu();
    private final Instant ablauf = Instant.parse("2026-11-22T10:30:00Z");

    @Test
    void eineErzeugteVorschauHatSnapshotHtmlUndAblauf() {
        var erzeugt = new Vorschauergebnis.Erzeugt(snapshot, "<p/>", ablauf);

        assertThat(erzeugt.snapshot()).isEqualTo(snapshot);
        assertThat(erzeugt.html()).isEqualTo("<p/>");
        assertThat(erzeugt.ablauf()).isEqualTo(ablauf);
    }

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void jedesErgebnisIstUnterscheidbar() {
        assertThat(java.util.stream.Stream.of(new Vorschauergebnis.Erzeugt(snapshot, "<p/>", ablauf),
                new Vorschauergebnis.StandardschreibenUnbekannt(), new Vorschauergebnis.BezugPasstNicht(),
                new Vorschauergebnis.KeineVeroeffentlichteVersion(), new Vorschauergebnis.NichtErlaubt())
                .map(VorschauergebnisTest::status))
                .containsExactly(200, 404, 422, 409, 403);
    }

    private static int status(Vorschauergebnis ergebnis) {
        return switch (ergebnis) {
            case Vorschauergebnis.Erzeugt erzeugt -> 200;
            case Vorschauergebnis.StandardschreibenUnbekannt unbekannt -> 404;
            case Vorschauergebnis.BezugPasstNicht passtNicht -> 422;
            case Vorschauergebnis.KeineVeroeffentlichteVersion keineVersion -> 409;
            case Vorschauergebnis.NichtErlaubt nichtErlaubt -> 403;
        };
    }

    @Test
    void pflichtattribute() {
        assertThatThrownBy(() -> new Vorschauergebnis.Erzeugt(null, "<p/>", ablauf)).hasMessageContaining("Snapshot");
        assertThatThrownBy(() -> new Vorschauergebnis.Erzeugt(snapshot, null, ablauf)).hasMessageContaining("HTML");
        assertThatThrownBy(() -> new Vorschauergebnis.Erzeugt(snapshot, "<p/>", null)).hasMessageContaining("Ablauf");
    }
}
