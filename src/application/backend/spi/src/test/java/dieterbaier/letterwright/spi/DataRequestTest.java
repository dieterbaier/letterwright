package dieterbaier.letterwright.spi;

import static dieterbaier.letterwright.spi.Checks.rejects;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DataRequestTest {

    private static final BusinessReference CUSTOMER = new BusinessReference("Kunde", "4711");
    private static final LetterTemplateId REMINDER =
            new LetterTemplateId(UUID.randomUUID(), "Mahnung");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void theUserIdRejectsMissingEmptyAndBlankValues(String value) {
        rejects(() -> new DataRequest(value, CUSTOMER, REMINDER), "Die Benutzerkennung");
    }

    @Test
    void referenceAndLetterTemplateAreRequired() {
        rejects(() -> new DataRequest("erika", null, REMINDER), "Der fachliche Bezug");
        rejects(() -> new DataRequest("erika", CUSTOMER, null), "Das Standardschreiben");
    }
}
