package dieterbaier.standardschreiben.domaene.modell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class PruefsummeTest {

    @Test
    void berechnetSha256AlsKleinbuchstabenHex() {
        // Bekannter Testvektor: SHA-256("abc")
        assertThat(Pruefsumme.ueber("abc".getBytes(StandardCharsets.US_ASCII)).hex())
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD",
        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015a",
        "za7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    })
    void lehntAndereFormateAb(String hex) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Pruefsumme(hex));
    }
}
