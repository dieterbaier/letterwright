package dieterbaier.letterwright.domain.model.template;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VersionNumberTest {

    @Test
    void versionNumbersStartAtOneAndAreOrdered() {
        assertThat(VersionNumber.FIRST.next()).isEqualTo(new VersionNumber(2));
        assertThat(VersionNumber.FIRST).isLessThan(new VersionNumber(2));
        rejects(() -> new VersionNumber(0), "VersionNumber");
        rejects(() -> new VersionNumber(-1), "VersionNumber");
    }
}
