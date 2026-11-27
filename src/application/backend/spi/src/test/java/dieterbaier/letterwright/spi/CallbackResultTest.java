package dieterbaier.letterwright.spi;

import static dieterbaier.letterwright.spi.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CallbackResultTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void aRejectionNeedsAReason(String reason) {
        rejects(() -> CallbackResult.rejected(reason), "Der Grund der Ablehnung");
    }

    @Test
    void callbackResultsAreValues() {
        assertThat(CallbackResult.accepted()).isEqualTo(new CallbackResult.Accepted());
        assertThat(CallbackResult.rejected("Aufgabe ist bereits erledigt"))
                .isEqualTo(new CallbackResult.Rejected("Aufgabe ist bereits erledigt"));
    }
}
