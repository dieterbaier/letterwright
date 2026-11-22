package dieterbaier.standardschreiben.adapter.out.host;

import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA_IM_SPI;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERSTELLT;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.KUNDE_IM_SPI;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.SCHREIBEN;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.nullVomHost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.schreiben.Loeschentscheidung;
import dieterbaier.standardschreiben.spi.Loeschanfrage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HostLoeschregelnTest {

    @Test
    void uebernehmenDieEntscheidungDesHosts() {
        List<Loeschanfrage> anfragen = new ArrayList<>();
        var regeln = new HostLoeschregeln(anfrage -> {
            anfragen.add(anfrage);
            return dieterbaier.standardschreiben.spi.Loeschentscheidung.gesperrt("Aufbewahrungsfrist bis 2036");
        });

        assertThat(regeln.entscheide(ERIKA, SCHREIBEN))
                .isEqualTo(Loeschentscheidung.gesperrt("Aufbewahrungsfrist bis 2036"));
        assertThat(anfragen)
                .containsExactly(new Loeschanfrage(ERIKA_IM_SPI, SCHREIBEN.id().wert(), KUNDE_IM_SPI, ERSTELLT));
    }

    @Test
    void nullVonDerLoeschrichtlinieIstEineVertragsverletzung() {
        var regeln = new HostLoeschregeln(anfrage -> nullVomHost());

        assertThatThrownBy(() -> regeln.entscheide(ERIKA, SCHREIBEN))
                .isInstanceOf(HostVertragVerletzt.class)
                .hasMessageContaining("null");
    }
}
