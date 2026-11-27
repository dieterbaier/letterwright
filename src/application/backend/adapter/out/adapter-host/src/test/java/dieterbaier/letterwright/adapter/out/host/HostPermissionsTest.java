package dieterbaier.letterwright.adapter.out.host;

import static dieterbaier.letterwright.adapter.out.host.TestData.ERIKA;
import static dieterbaier.letterwright.adapter.out.host.TestData.ERIKA_IN_SPI;
import static dieterbaier.letterwright.adapter.out.host.TestData.CUSTOMER;
import static dieterbaier.letterwright.adapter.out.host.TestData.CUSTOMER_IN_SPI;
import static dieterbaier.letterwright.adapter.out.host.TestData.REMINDER;
import static dieterbaier.letterwright.adapter.out.host.TestData.REMINDER_IN_SPI;
import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.TemplateAction;
import dieterbaier.letterwright.spi.AuthorizationRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HostPermissionsTest {

    @Test
    void passTheHostsAnswerThrough() {
        List<AuthorizationRequest> requests = new ArrayList<>();
        var permissions = new HostPermissions(request -> {
            requests.add(request);
            return request instanceof AuthorizationRequest.ForCreation;
        });

        assertThat(permissions.mayCreate(ERIKA, CUSTOMER, REMINDER)).isTrue();
        assertThat(permissions.may(ERIKA, LetterAction.DELETE, CUSTOMER)).isFalse();
        assertThat(permissions.may(ERIKA, TemplateAction.PUBLISH)).isFalse();
        assertThat(requests).containsExactly(
                new AuthorizationRequest.ForCreation(ERIKA_IN_SPI, CUSTOMER_IN_SPI, REMINDER_IN_SPI),
                new AuthorizationRequest.ForLetter(ERIKA_IN_SPI,
                        dieterbaier.letterwright.spi.LetterAction.DELETE, CUSTOMER_IN_SPI),
                new AuthorizationRequest.ForTemplates(ERIKA_IN_SPI,
                        dieterbaier.letterwright.spi.TemplateAction.PUBLISH));
    }
}
