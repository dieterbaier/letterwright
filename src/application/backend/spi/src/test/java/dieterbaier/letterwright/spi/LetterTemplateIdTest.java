package dieterbaier.letterwright.spi;

import static dieterbaier.letterwright.spi.Checks.rejects;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class LetterTemplateIdTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void theNameRejectsMissingEmptyAndBlankValues(String value) {
        rejects(() -> new LetterTemplateId(UUID.randomUUID(), value), "The name of the letter template");
    }

    @Test
    void theIdIsRequired() {
        rejects(() -> new LetterTemplateId(null, "Payment reminder"), "The ID of the letter template");
    }
}
