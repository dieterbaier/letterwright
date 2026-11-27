package dieterbaier.letterwright.spi;

import static dieterbaier.letterwright.spi.Checks.rejects;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class BusinessReferenceTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void typeAndIdRejectMissingEmptyAndBlankValues(String value) {
        rejects(() -> new BusinessReference(value, "4711"), "Der Typ des fachlichen Bezugs");
        rejects(() -> new BusinessReference("Kunde", value), "Die ID des fachlichen Bezugs");
    }
}
