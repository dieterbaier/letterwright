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
        rejects(() -> new BusinessReference(value, "4711"), "type of the business reference");
        rejects(() -> new BusinessReference("Customer", value), "ID of the business reference");
    }

    @Test
    void isValueEqual() {
        assertThat(new BusinessReference("Customer", "4711")).isEqualTo(new BusinessReference("Customer", "4711"))
                .isNotEqualTo(new BusinessReference("Case", "4711"));
    }
}
