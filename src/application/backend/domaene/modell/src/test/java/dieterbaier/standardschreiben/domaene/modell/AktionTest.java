package dieterbaier.standardschreiben.domaene.modell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AktionTest {

    @Test
    void nurDiePflegeDerVorlagenHatKeinenFachlichenBezug() {
        assertThat(Arrays.stream(Aktion.values()).filter(aktion -> !aktion.brauchtBezug()))
                .containsExactly(Aktion.VORLAGEN_PFLEGEN);
    }
}
