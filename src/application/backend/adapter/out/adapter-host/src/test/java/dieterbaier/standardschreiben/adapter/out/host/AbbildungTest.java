package dieterbaier.standardschreiben.adapter.out.host;

import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.KUNDE;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.MAHNUNG;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.SCHREIBEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.Aktion;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.Loeschentscheidung;
import dieterbaier.standardschreiben.spi.Datenanfrage;
import dieterbaier.standardschreiben.spi.Loeschanfrage;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AbbildungTest {

    @Test
    void derFachlicheBezugGehtInBeideRichtungenVerlustfrei() {
        dieterbaier.standardschreiben.spi.FachlicherBezug imSpi = Abbildung.zumSpi(KUNDE);

        assertThat(imSpi).isEqualTo(new dieterbaier.standardschreiben.spi.FachlicherBezug("Kunde", "4711"));
        assertThat(Abbildung.zurDomaene(imSpi)).isEqualTo(KUNDE);
    }

    @ParameterizedTest
    @EnumSource(Aktion.class)
    void jedeAktionGehtInBeideRichtungenAufDieGleichnamige(Aktion aktion) {
        dieterbaier.standardschreiben.spi.Aktion imSpi = Abbildung.zumSpi(aktion);

        assertThat(imSpi.name()).isEqualTo(aktion.name());
        assertThat(imSpi.hatBezug()).isEqualTo(aktion.brauchtBezug());
        assertThat(Abbildung.zurDomaene(imSpi)).isEqualTo(aktion);
    }

    @ParameterizedTest
    @EnumSource(Loeschentscheidung.class)
    void jedeLoeschentscheidungGehtInBeideRichtungenAufDieGleichnamige(Loeschentscheidung entscheidung) {
        dieterbaier.standardschreiben.spi.Loeschentscheidung imSpi = Abbildung.zumSpi(entscheidung);

        assertThat(imSpi.name()).isEqualTo(entscheidung.name());
        assertThat(Abbildung.zurDomaene(imSpi)).isEqualTo(entscheidung);
    }

    @Test
    void beideSeitenKennenDieselbenWerte() {
        assertThat(namen(dieterbaier.standardschreiben.spi.Aktion.values())).isEqualTo(namen(Aktion.values()));
        assertThat(namen(dieterbaier.standardschreiben.spi.Loeschentscheidung.values()))
                .isEqualTo(namen(Loeschentscheidung.values()));
    }

    @Test
    void derDatenkontextGehtInBeideRichtungenVerlustfrei() {
        Map<String, Object> werte = Map.of("name", "Erika", "posten", List.of(Map.of("betrag", "12,00 €")));
        Datenkontext inDerDomaene = Datenkontext.aus(werte);

        dieterbaier.standardschreiben.spi.Datenkontext imSpi = Abbildung.zumSpi(inDerDomaene);

        assertThat(imSpi.werte()).isEqualTo(werte);
        assertThat(Abbildung.zurDomaene(imSpi)).isEqualTo(inDerDomaene);
    }

    @Test
    void einUngueltigerDatenkontextDesHostsIstEineVertragsverletzung() {
        var ungueltig = new dieterbaier.standardschreiben.spi.Datenkontext(Map.of("betrag", 12));

        assertThatThrownBy(() -> Abbildung.zurDomaene(ungueltig))
                .isInstanceOf(HostVertragVerletzt.class)
                .hasMessageContaining("PlatzhalterdatenAnbieter")
                .hasCauseInstanceOf(Validierungsfehler.class);
    }

    @Test
    void dieDatenanfrageTraegtBenutzerBezugUndStandardschreiben() {
        Datenanfrage anfrage = Abbildung.datenanfrage(ERIKA, KUNDE, MAHNUNG);

        assertThat(anfrage.benutzerkennung()).isEqualTo("erika");
        assertThat(anfrage.bezug()).isEqualTo(Abbildung.zumSpi(KUNDE));
        assertThat(anfrage.standardschreiben().id()).isEqualTo(MAHNUNG.id().wert());
        assertThat(anfrage.standardschreiben().name()).isEqualTo("Mahnung");
    }

    @Test
    void dieLoeschanfrageTraegtSchreibenBezugUndErstellungszeitpunkt() {
        Loeschanfrage anfrage = Abbildung.loeschanfrage(ERIKA, SCHREIBEN);

        assertThat(anfrage.benutzerkennung()).isEqualTo("erika");
        assertThat(anfrage.schreiben()).isEqualTo(SCHREIBEN.id().wert());
        assertThat(anfrage.bezug()).isEqualTo(Abbildung.zumSpi(KUNDE));
        assertThat(anfrage.erstellt()).isEqualTo(SCHREIBEN.erstellt());
    }

    private static List<String> namen(Enum<?>[] werte) {
        return Arrays.stream(werte).map(Enum::name).toList();
    }
}
