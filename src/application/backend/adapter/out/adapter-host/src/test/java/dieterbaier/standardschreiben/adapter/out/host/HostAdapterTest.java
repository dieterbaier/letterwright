package dieterbaier.standardschreiben.adapter.out.host;

import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.KUNDE;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.MAHNUNG;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.SCHREIBEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Loeschentscheidung;
import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Datenanfrage;
import dieterbaier.standardschreiben.spi.Datenkontext;
import dieterbaier.standardschreiben.spi.Loeschanfrage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HostAdapterTest {

    @Test
    void diePlatzhalterdatenquelleFragtDenHostUndUebernimmtSeineWerte() {
        List<Datenanfrage> anfragen = new ArrayList<>();
        var quelle = new HostPlatzhalterdatenquelle(anfrage -> {
            anfragen.add(anfrage);
            return new Datenkontext(Map.of("name", "Erika"));
        });

        var kontext = quelle.liesDatenkontext(ERIKA, KUNDE, MAHNUNG);

        assertThat(kontext.werte()).isEqualTo(Map.of("name", "Erika"));
        assertThat(anfragen).containsExactly(Abbildung.datenanfrage(ERIKA, KUNDE, MAHNUNG));
    }

    @Test
    void nullVomPlatzhalterdatenAnbieterIstEineVertragsverletzung() {
        var quelle = new HostPlatzhalterdatenquelle(anfrage -> nullVomHost());

        assertThatThrownBy(() -> quelle.liesDatenkontext(ERIKA, KUNDE, MAHNUNG))
                .isInstanceOf(HostVertragVerletzt.class)
                .hasMessageContaining("null");
    }

    @Test
    void dieBerechtigungenReichenDieAntwortDesHostsDurch() {
        List<Berechtigungsanfrage> anfragen = new ArrayList<>();
        var berechtigungen = new HostBerechtigungen(anfrage -> {
            anfragen.add(anfrage);
            return anfrage.aktion() == dieterbaier.standardschreiben.spi.SchreibenAktion.ERZEUGEN;
        });

        assertThat(berechtigungen.darf(ERIKA, SchreibenAktion.ERZEUGEN, KUNDE)).isTrue();
        assertThat(berechtigungen.darf(ERIKA, SchreibenAktion.LOESCHEN, KUNDE)).isFalse();
        assertThat(berechtigungen.darf(ERIKA, VorlagenAktion.VEROEFFENTLICHEN)).isFalse();
        assertThat(anfragen).containsExactly(
                Berechtigungsanfrage.zuSchreiben("erika", dieterbaier.standardschreiben.spi.SchreibenAktion.ERZEUGEN,
                        Abbildung.zumSpi(KUNDE)),
                Berechtigungsanfrage.zuSchreiben("erika", dieterbaier.standardschreiben.spi.SchreibenAktion.LOESCHEN,
                        Abbildung.zumSpi(KUNDE)),
                Berechtigungsanfrage.zuVorlagen("erika",
                        dieterbaier.standardschreiben.spi.VorlagenAktion.VEROEFFENTLICHEN));
    }

    @Test
    void dieLoeschregelnUebernehmenDieEntscheidungDesHosts() {
        List<Loeschanfrage> anfragen = new ArrayList<>();
        var regeln = new HostLoeschregeln(anfrage -> {
            anfragen.add(anfrage);
            return dieterbaier.standardschreiben.spi.Loeschentscheidung.gesperrt("Aufbewahrungsfrist bis 2036");
        });

        assertThat(regeln.entscheide(ERIKA, SCHREIBEN))
                .isEqualTo(Loeschentscheidung.gesperrt("Aufbewahrungsfrist bis 2036"));
        assertThat(anfragen).containsExactly(Abbildung.loeschanfrage(ERIKA, SCHREIBEN));
    }

    @Test
    void nullVonDerLoeschrichtlinieIstEineVertragsverletzung() {
        var regeln = new HostLoeschregeln(anfrage -> nullVomHost());

        assertThatThrownBy(() -> regeln.entscheide(ERIKA, SCHREIBEN))
                .isInstanceOf(HostVertragVerletzt.class)
                .hasMessageContaining("null");
    }

    /** Ein Host, der sich nicht an den Null-Vertrag hält. */
    @SuppressWarnings({"NullAway", "unchecked"})
    private static <T> T nullVomHost() {
        return (T) null;
    }
}
