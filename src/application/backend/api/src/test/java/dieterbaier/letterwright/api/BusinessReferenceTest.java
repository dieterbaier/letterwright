package dieterbaier.letterwright.api;

import static dieterbaier.letterwright.api.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class BusinessReferenceTest {

    @Test
    void carriesTypeAndId() {
        var reference = new BusinessReference("Kunde", "4711");

        assertThat(reference.type()).isEqualTo("Kunde");
        assertThat(reference.id()).isEqualTo("4711");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void typeAndIdRejectMissingEmptyAndBlankValues(String value) {
        rejects(() -> new BusinessReference(value, "4711"), "Der Typ des fachlichen Bezugs");
        rejects(() -> new BusinessReference("Kunde", value), "Die ID des fachlichen Bezugs");
    }
}
