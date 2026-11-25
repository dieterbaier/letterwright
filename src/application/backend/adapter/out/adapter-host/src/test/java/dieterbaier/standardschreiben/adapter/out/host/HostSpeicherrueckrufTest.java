package dieterbaier.standardschreiben.adapter.out.host;

import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA_IM_SPI;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERSTELLT;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.KUNDE_IM_SPI;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.MAHNUNG;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.SCHREIBEN;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.nullVomHost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.schreiben.Rueckrufergebnis;
import dieterbaier.standardschreiben.spi.GespeichertesSchreiben;
import dieterbaier.standardschreiben.spi.Standardschreibenversion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HostSpeicherrueckrufTest {

    @Test
    void derHostErhaeltDieIdentifikatorenDesNeuenSchreibens() {
        List<GespeichertesSchreiben> erhalten = new ArrayList<>();
        var rueckruf = HostSpeicherrueckruf.zu(schreiben -> {
            erhalten.add(schreiben);
            return dieterbaier.standardschreiben.spi.Rueckrufergebnis.angenommen();
        });

        assertThat(rueckruf.angelegt(ERIKA, SCHREIBEN)).isEqualTo(Rueckrufergebnis.angenommen());
        assertThat(erhalten).containsExactly(new GespeichertesSchreiben(SCHREIBEN.id().wert(), KUNDE_IM_SPI,
                new Standardschreibenversion(MAHNUNG.id().wert(), 1), ERIKA_IM_SPI, ERSTELLT));
    }

    @Test
    void dieAblehnungDesHostsKommtMitIhremGrund() {
        var rueckruf = HostSpeicherrueckruf.zu(
                schreiben -> dieterbaier.standardschreiben.spi.Rueckrufergebnis.abgelehnt("Aufgabe ist erledigt"));

        assertThat(rueckruf.angelegt(ERIKA, SCHREIBEN)).isEqualTo(Rueckrufergebnis.abgelehnt("Aufgabe ist erledigt"));
    }

    @Test
    void nullVomRueckrufIstEineVertragsverletzung() {
        var rueckruf = HostSpeicherrueckruf.zu(schreiben -> nullVomHost());

        assertThatThrownBy(() -> rueckruf.angelegt(ERIKA, SCHREIBEN))
                .isInstanceOf(HostVertragVerletzt.class)
                .hasMessageContaining("null");
    }

    @Test
    void eineAusnahmeDesHostsBleibtUnveraendert() {
        var fehler = new IllegalStateException("Aufgabenverwaltung nicht erreichbar");
        var rueckruf = HostSpeicherrueckruf.zu(schreiben -> {
            throw fehler;
        });

        assertThatThrownBy(() -> rueckruf.angelegt(ERIKA, SCHREIBEN)).isSameAs(fehler);
    }

    @Test
    void ohneRueckrufIstJedesSchreibenAngenommen() {
        assertThat(HostSpeicherrueckruf.ohne().angelegt(ERIKA, SCHREIBEN)).isEqualTo(Rueckrufergebnis.angenommen());
    }
}
