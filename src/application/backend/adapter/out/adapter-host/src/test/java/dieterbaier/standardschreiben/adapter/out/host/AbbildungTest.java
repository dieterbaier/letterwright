package dieterbaier.standardschreiben.adapter.out.host;

import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.KUNDE;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.MAHNUNG;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.SCHREIBEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Loeschentscheidung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Rueckrufergebnis;
import dieterbaier.standardschreiben.spi.Datenanfrage;
import dieterbaier.standardschreiben.spi.Loeschanfrage;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.FieldSource;

class AbbildungTest {

    @Test
    void derFachlicheBezugGehtInBeideRichtungenVerlustfrei() {
        dieterbaier.standardschreiben.spi.FachlicherBezug imSpi = Abbildung.zumSpi(KUNDE);

        assertThat(imSpi).isEqualTo(new dieterbaier.standardschreiben.spi.FachlicherBezug("Kunde", "4711"));
        assertThat(Abbildung.zurDomaene(imSpi)).isEqualTo(KUNDE);
    }

    @ParameterizedTest
    @EnumSource(VorlagenAktion.class)
    void jedeAktionAnVorlagenGehtAufDieGleichnamige(VorlagenAktion aktion) {
        assertThat(Abbildung.zumSpi(aktion).name()).isEqualTo(aktion.name());
    }

    @ParameterizedTest
    @EnumSource(SchreibenAktion.class)
    void jedeAktionAnSchreibenGehtAufDieGleichnamige(SchreibenAktion aktion) {
        assertThat(Abbildung.zumSpi(aktion).name()).isEqualTo(aktion.name());
    }

    static final List<Loeschentscheidung> LOESCHENTSCHEIDUNGEN = List.of(Loeschentscheidung.hart(),
            Loeschentscheidung.logisch(), Loeschentscheidung.gesperrt("Aufbewahrungsfrist bis 2036"));

    @ParameterizedTest
    @FieldSource("LOESCHENTSCHEIDUNGEN")
    void jedeLoeschentscheidungGehtInBeideRichtungenVerlustfrei(Loeschentscheidung entscheidung) {
        dieterbaier.standardschreiben.spi.Loeschentscheidung imSpi = Abbildung.zumSpi(entscheidung);

        assertThat(imSpi.getClass().getSimpleName()).isEqualTo(entscheidung.getClass().getSimpleName());
        assertThat(Abbildung.zurDomaene(imSpi)).isEqualTo(entscheidung);
    }

    @Test
    void jedesRueckrufergebnisDesHostsGehtAufDasGleichnamige() {
        assertThat(Abbildung.zurDomaene(dieterbaier.standardschreiben.spi.Rueckrufergebnis.angenommen()))
                .isEqualTo(Rueckrufergebnis.angenommen());
        assertThat(Abbildung.zurDomaene(dieterbaier.standardschreiben.spi.Rueckrufergebnis.abgelehnt("Erledigt")))
                .isEqualTo(Rueckrufergebnis.abgelehnt("Erledigt"));
    }

    @Test
    void derGrundDerSperreBleibtErhalten() {
        assertThat(Abbildung.zumSpi(Loeschentscheidung.gesperrt("Frist")))
                .isEqualTo(dieterbaier.standardschreiben.spi.Loeschentscheidung.gesperrt("Frist"));
    }

    @Test
    void beideSeitenKennenDieselbenWerte() {
        assertThat(namen(dieterbaier.standardschreiben.spi.VorlagenAktion.values()))
                .isEqualTo(namen(VorlagenAktion.values()));
        assertThat(namen(dieterbaier.standardschreiben.spi.SchreibenAktion.values()))
                .isEqualTo(namen(SchreibenAktion.values()));
        assertThat(arten(dieterbaier.standardschreiben.spi.Loeschentscheidung.class))
                .isEqualTo(arten(Loeschentscheidung.class));
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

    private static List<String> arten(Class<?> versiegelt) {
        return Arrays.stream(versiegelt.getPermittedSubclasses()).map(Class::getSimpleName).toList();
    }

    private static List<String> namen(Enum<?>[] werte) {
        return Arrays.stream(werte).map(Enum::name).toList();
    }
}
