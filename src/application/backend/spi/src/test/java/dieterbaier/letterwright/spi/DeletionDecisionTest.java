package dieterbaier.letterwright.spi;

import static dieterbaier.letterwright.spi.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DeletionDecisionTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void aDeletionBlockNeedsAReason(String reason) {
        rejects(() -> DeletionDecision.blocked(reason), "The reason for the deletion block");
    }

    @Test
    void deletionDecisionsAreValues() {
        assertThat(DeletionDecision.hard()).isEqualTo(new DeletionDecision.Hard());
        assertThat(DeletionDecision.logical()).isEqualTo(new DeletionDecision.Logical());
        assertThat(DeletionDecision.blocked("Retention period")).isEqualTo(new DeletionDecision.Blocked("Retention period"));
    }
}
