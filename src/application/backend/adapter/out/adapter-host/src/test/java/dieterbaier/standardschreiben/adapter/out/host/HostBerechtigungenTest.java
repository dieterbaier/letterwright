package dieterbaier.standardschreiben.adapter.out.host;

import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA_IM_SPI;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.KUNDE;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.KUNDE_IM_SPI;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.MAHNUNG;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.MAHNUNG_IM_SPI;
import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HostBerechtigungenTest {

    @Test
    void reichenDieAntwortDesHostsDurch() {
        List<Berechtigungsanfrage> anfragen = new ArrayList<>();
        var berechtigungen = new HostBerechtigungen(anfrage -> {
            anfragen.add(anfrage);
            return anfrage instanceof Berechtigungsanfrage.ZumErzeugen;
        });

        assertThat(berechtigungen.darfErzeugen(ERIKA, KUNDE, MAHNUNG)).isTrue();
        assertThat(berechtigungen.darf(ERIKA, SchreibenAktion.LOESCHEN, KUNDE)).isFalse();
        assertThat(berechtigungen.darf(ERIKA, VorlagenAktion.VEROEFFENTLICHEN)).isFalse();
        assertThat(anfragen).containsExactly(
                new Berechtigungsanfrage.ZumErzeugen(ERIKA_IM_SPI, KUNDE_IM_SPI, MAHNUNG_IM_SPI),
                new Berechtigungsanfrage.ZuSchreiben(ERIKA_IM_SPI,
                        dieterbaier.standardschreiben.spi.SchreibenAktion.LOESCHEN, KUNDE_IM_SPI),
                new Berechtigungsanfrage.ZuVorlagen(ERIKA_IM_SPI,
                        dieterbaier.standardschreiben.spi.VorlagenAktion.VEROEFFENTLICHEN));
    }
}
