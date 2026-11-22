package dieterbaier.standardschreiben.adapter.out.host;

import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.ERIKA_IM_SPI;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.KUNDE;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.KUNDE_IM_SPI;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.MAHNUNG;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.MAHNUNG_IM_SPI;
import static dieterbaier.standardschreiben.adapter.out.host.Testdaten.nullVomHost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.spi.Datenanfrage;
import dieterbaier.standardschreiben.spi.Datenkontext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HostPlatzhalterdatenquelleTest {

    @Test
    void fragtDenHostUndUebernimmtSeineWerte() {
        List<Datenanfrage> anfragen = new ArrayList<>();
        var quelle = new HostPlatzhalterdatenquelle(anfrage -> {
            anfragen.add(anfrage);
            return new Datenkontext(Map.of("name", "Erika"));
        });

        var kontext = quelle.liesDatenkontext(ERIKA, KUNDE, MAHNUNG);

        assertThat(kontext.werte()).isEqualTo(Map.of("name", "Erika"));
        assertThat(anfragen).containsExactly(new Datenanfrage(ERIKA_IM_SPI, KUNDE_IM_SPI, MAHNUNG_IM_SPI));
    }

    @Test
    void nullVomPlatzhalterdatenAnbieterIstEineVertragsverletzung() {
        var quelle = new HostPlatzhalterdatenquelle(anfrage -> nullVomHost());

        assertThatThrownBy(() -> quelle.liesDatenkontext(ERIKA, KUNDE, MAHNUNG))
                .isInstanceOf(HostVertragVerletzt.class)
                .hasMessageContaining("null");
    }
}
