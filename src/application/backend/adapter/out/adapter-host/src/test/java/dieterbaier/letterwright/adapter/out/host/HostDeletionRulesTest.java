package dieterbaier.letterwright.adapter.out.host;

import static dieterbaier.letterwright.adapter.out.host.TestData.ERIKA;
import static dieterbaier.letterwright.adapter.out.host.TestData.ERIKA_IN_SPI;
import static dieterbaier.letterwright.adapter.out.host.TestData.CREATED;
import static dieterbaier.letterwright.adapter.out.host.TestData.CUSTOMER_IN_SPI;
import static dieterbaier.letterwright.adapter.out.host.TestData.LETTER;
import static dieterbaier.letterwright.adapter.out.host.TestData.nullFromHost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.letter.DeletionDecision;
import dieterbaier.letterwright.spi.DeletionRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HostDeletionRulesTest {

    @Test
    void adoptTheHostsDecision() {
        List<DeletionRequest> requests = new ArrayList<>();
        var rules = new HostDeletionRules(request -> {
            requests.add(request);
            return dieterbaier.letterwright.spi.DeletionDecision.blocked("Retention period until 2036");
        });

        assertThat(rules.decide(ERIKA, LETTER))
                .isEqualTo(DeletionDecision.blocked("Retention period until 2036"));
        assertThat(requests)
                .containsExactly(new DeletionRequest(ERIKA_IN_SPI, LETTER.id().value(), CUSTOMER_IN_SPI, CREATED));
    }

    @Test
    void nullFromTheDeletionPolicyIsAContractViolation() {
        var rules = new HostDeletionRules(request -> nullFromHost());

        assertThatThrownBy(() -> rules.decide(ERIKA, LETTER))
                .isInstanceOf(HostContractViolated.class)
                .hasMessageContaining("null");
    }
}
