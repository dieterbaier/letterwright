package dieterbaier.letterwright.domain.model;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ChecksumTest {

    @Test
    void computesSha256AsLowercaseHex() {
        // Bekannter Testvektor: SHA-256("abc")
        assertThat(Checksum.of("abc".getBytes(StandardCharsets.US_ASCII)).hex())
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "",
        "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD",
        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015a",
        "za7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    })
    void rejectsMissingAndOtherFormats(String hex) {
        rejects(() -> new Checksum(hex), "Prüfsumme");
    }

    @Test
    void requiresContentToCompute() {
        rejects(() -> Checksum.of(null), "Inhalt");
    }
}
