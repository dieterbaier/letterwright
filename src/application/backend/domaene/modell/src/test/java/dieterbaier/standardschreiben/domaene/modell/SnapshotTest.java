package dieterbaier.standardschreiben.domaene.modell;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SnapshotTest {

    static final Instant JETZT = Instant.parse("2026-11-21T10:00:00Z");
    static final BenutzerKennung SACHBEARBEITER = new BenutzerKennung("erika");
    static final FachlicherBezug KUNDE = new FachlicherBezug("Kunde", "4711");

    static Snapshot snapshot(Datenkontext datenkontext) {
        return Snapshot.anlegen(SnapshotId.neu(), SACHBEARBEITER, KUNDE,
                new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE),
                new Versionsverweis(VorlagenId.neu(), new Versionsnummer(3)),
                datenkontext, JETZT);
    }

    private final Snapshot snapshot = snapshot(Datenkontext.aus(Map.of("name", "Erika")));

    @Test
    void laeuftDreissigMinutenNachDerAnlageAb() {
        assertThat(snapshot.ablauf()).isEqualTo(JETZT.plus(Duration.ofMinutes(30)));
        assertThat(snapshot.istAbgelaufen(JETZT.plus(Duration.ofMinutes(30)).minusMillis(1))).isFalse();
        assertThat(snapshot.istAbgelaufen(JETZT.plus(Duration.ofMinutes(30)))).isTrue();
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
    void erkenntEinenGeaendertenDatenkontext() {
        assertThat(snapshot.hatUnveraendertenDatenkontext(Datenkontext.aus(Map.of("name", "Erika")))).isTrue();
        assertThat(snapshot.hatUnveraendertenDatenkontext(Datenkontext.aus(Map.of("name", "Max")))).isFalse();
    }
}
