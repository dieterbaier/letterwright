package dieterbaier.letterwright.domain.model.letter;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DeletionDecisionTest {

    @Test
    void aBlockCarriesItsReason() {
        assertThat(DeletionDecision.blocked("Aufbewahrungsfrist bis 2036"))
                .isEqualTo(new DeletionDecision.Blocked("Aufbewahrungsfrist bis 2036"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void aBlockWithoutReasonIsNotAllowed(String reason) {
        rejects(() -> DeletionDecision.blocked(reason), "Grund der Löschsperre");
    }

    @Test
    void hardAndLogicalAreValues() {
        assertThat(DeletionDecision.hard()).isEqualTo(new DeletionDecision.Hard());
        assertThat(DeletionDecision.logical()).isEqualTo(new DeletionDecision.Logical());
        assertThat(DeletionDecision.hard()).isNotEqualTo(DeletionDecision.logical());
    }
}
