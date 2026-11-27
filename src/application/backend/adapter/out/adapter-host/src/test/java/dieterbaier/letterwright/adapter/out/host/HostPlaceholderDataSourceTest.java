package dieterbaier.letterwright.adapter.out.host;

import static dieterbaier.letterwright.adapter.out.host.TestData.ERIKA;
import static dieterbaier.letterwright.adapter.out.host.TestData.ERIKA_IN_SPI;
import static dieterbaier.letterwright.adapter.out.host.TestData.CUSTOMER;
import static dieterbaier.letterwright.adapter.out.host.TestData.CUSTOMER_IN_SPI;
import static dieterbaier.letterwright.adapter.out.host.TestData.REMINDER;
import static dieterbaier.letterwright.adapter.out.host.TestData.REMINDER_IN_SPI;
import static dieterbaier.letterwright.adapter.out.host.TestData.nullFromHost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.spi.DataRequest;
import dieterbaier.letterwright.spi.DataContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HostPlaceholderDataSourceTest {

    @Test
    void asksTheHostAndAdoptsItsValues() {
        List<DataRequest> requests = new ArrayList<>();
        var source = new HostPlaceholderDataSource(request -> {
            requests.add(request);
            return new DataContext(Map.of("name", "Erika"));
        });

        var context = source.readDataContext(ERIKA, CUSTOMER, REMINDER);

        assertThat(context.values()).isEqualTo(Map.of("name", "Erika"));
        assertThat(requests).containsExactly(new DataRequest(ERIKA_IN_SPI, CUSTOMER_IN_SPI, REMINDER_IN_SPI));
    }

    @Test
    void nullFromThePlaceholderDataProviderIsAContractViolation() {
        var source = new HostPlaceholderDataSource(request -> nullFromHost());

        assertThatThrownBy(() -> source.readDataContext(ERIKA, CUSTOMER, REMINDER))
                .isInstanceOf(HostContractViolated.class)
                .hasMessageContaining("null");
    }
}
