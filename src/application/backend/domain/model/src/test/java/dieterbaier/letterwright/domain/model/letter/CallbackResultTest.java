package dieterbaier.letterwright.domain.model.letter;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CallbackResultTest {

    @Test
    void aRejectionCarriesItsReason() {
        assertThat(CallbackResult.rejected("Task is already done"))
                .isEqualTo(new CallbackResult.Rejected("Task is already done"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void aRejectionWithoutReasonIsNotAllowed(String reason) {
        rejects(() -> CallbackResult.rejected(reason), "reason for the rejection");
    }

    @Test
    void acceptedIsAValue() {
        assertThat(CallbackResult.accepted()).isEqualTo(new CallbackResult.Accepted());
    }
}
