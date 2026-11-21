package dieterbaier.standardschreiben.adapter.out.host;

import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.KUNDE;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.MAHNUNG;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.SCHREIBEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.Aktion;
import dieterbaier.standardschreiben.domaene.modell.Loeschentscheidung;
import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Datenanfrage;
import dieterbaier.standardschreiben.spi.Datenkontext;
import dieterbaier.standardschreiben.spi.Loeschanfrage;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;
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
            return anfrage.aktion() == dieterbaier.standardschreiben.spi.Aktion.SCHREIBEN_ERZEUGEN;
        });

        assertThat(berechtigungen.darf(ERIKA, Aktion.SCHREIBEN_ERZEUGEN, KUNDE)).isTrue();
        assertThat(berechtigungen.darf(ERIKA, Aktion.SCHREIBEN_LOESCHEN, KUNDE)).isFalse();
        assertThat(berechtigungen.darf(ERIKA, Aktion.VORLAGEN_VEROEFFENTLICHEN)).isFalse();
        assertThat(anfragen).containsExactly(
                new Berechtigungsanfrage("erika", dieterbaier.standardschreiben.spi.Aktion.SCHREIBEN_ERZEUGEN,
                        Abbildung.zumSpi(KUNDE)),
                new Berechtigungsanfrage("erika", dieterbaier.standardschreiben.spi.Aktion.SCHREIBEN_LOESCHEN,
                        Abbildung.zumSpi(KUNDE)),
                new Berechtigungsanfrage("erika", dieterbaier.standardschreiben.spi.Aktion.VORLAGEN_VEROEFFENTLICHEN,
                        null));
    }

    @Test
    void aktionenFuerVorlagenGehenNichtMitBezug() {
        var berechtigungen = new HostBerechtigungen(anfrage -> true);

        assertThatThrownBy(() -> berechtigungen.darf(ERIKA, Aktion.VORLAGEN_BEARBEITEN, KUNDE))
                .isInstanceOf(Validierungsfehler.class)
                .hasMessageContaining("VORLAGEN_BEARBEITEN");
    }

    @Test
    void aktionenFuerSchreibenGehenNichtOhneBezug() {
        var berechtigungen = new HostBerechtigungen(anfrage -> true);

        assertThatThrownBy(() -> berechtigungen.darf(ERIKA, Aktion.SCHREIBEN_ANSEHEN))
                .isInstanceOf(Validierungsfehler.class)
                .hasMessageContaining("SCHREIBEN_ANSEHEN");
    }

    @Test
    void dieLoeschregelnUebernehmenDieEntscheidungDesHosts() {
        List<Loeschanfrage> anfragen = new ArrayList<>();
        var regeln = new HostLoeschregeln(anfrage -> {
            anfragen.add(anfrage);
            return dieterbaier.standardschreiben.spi.Loeschentscheidung.LOGISCH;
        });

        assertThat(regeln.entscheide(ERIKA, SCHREIBEN)).isEqualTo(Loeschentscheidung.LOGISCH);
        assertThat(anfragen).containsExactly(Abbildung.loeschanfrage(ERIKA, SCHREIBEN));
    }

    @Test
    void nullVonDerLoeschrichtlinieIstEineVertragsverletzung() {
        var regeln = new HostLoeschregeln(anfrage -> nullVomHost());

        assertThatThrownBy(() -> regeln.entscheide(ERIKA, SCHREIBEN))
                .isInstanceOf(HostVertragVerletzt.class)
                .hasMessageContaining("null");
    }

    @Test
    void ohneSpiImplementierungLaesstSichKeinAdapterBauen() {
        assertThatThrownBy(() -> new HostPlatzhalterdatenquelle(nullVomHost()))
                .isInstanceOf(Validierungsfehler.class);
        assertThatThrownBy(() -> new HostBerechtigungen(nullVomHost())).isInstanceOf(Validierungsfehler.class);
        assertThatThrownBy(() -> new HostLoeschregeln(nullVomHost())).isInstanceOf(Validierungsfehler.class);
    }

    /** Ein Host, der sich nicht an den Null-Vertrag hält. */
    @SuppressWarnings({"NullAway", "unchecked"})
    private static <T> T nullVomHost() {
        return (T) null;
    }
}
