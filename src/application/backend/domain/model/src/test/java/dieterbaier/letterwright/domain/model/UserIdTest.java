package dieterbaier.letterwright.domain.model;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class UserIdTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsMissingEmptyAndBlankValues(String value) {
        rejects(() -> new UserId(value), "Benutzerkennung");
    }

    @Test
    void isValueEqual() {
        assertThat(new UserId("erika")).isEqualTo(new UserId("erika"))
                .isNotEqualTo(new UserId("max"));
    }
}
