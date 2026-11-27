package dieterbaier.letterwright.domain.model;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class BusinessReferenceTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void typeAndIdRejectMissingEmptyAndBlankValues(String value) {
        rejects(() -> new BusinessReference(value, "4711"), "Typ des fachlichen Bezugs");
        rejects(() -> new BusinessReference("Kunde", value), "ID des fachlichen Bezugs");
    }

    @Test
    void isValueEqual() {
        assertThat(new BusinessReference("Kunde", "4711")).isEqualTo(new BusinessReference("Kunde", "4711"))
                .isNotEqualTo(new BusinessReference("Vorgang", "4711"));
    }
}
