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
        var reference = new BusinessReference("Customer", "4711");

        assertThat(reference.type()).isEqualTo("Customer");
        assertThat(reference.id()).isEqualTo("4711");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void typeAndIdRejectMissingEmptyAndBlankValues(String value) {
        rejects(() -> new BusinessReference(value, "4711"), "The type of the business reference");
        rejects(() -> new BusinessReference("Customer", value), "The ID of the business reference");
    }
}
