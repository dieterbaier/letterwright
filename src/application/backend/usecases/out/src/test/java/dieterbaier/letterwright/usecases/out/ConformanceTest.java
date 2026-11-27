package dieterbaier.letterwright.usecases.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConformanceTest {

    @Test
    void aNonConformantPdfHasItsFindings() {
        var findings = new ArrayList<>(List.of("PDF/UA-1: Image without alternative text"));
        var notConformant = (Conformance.NotConformant) Conformance.notConformant(findings);
        findings.clear();

        assertThat(notConformant.findings()).containsExactly("PDF/UA-1: Image without alternative text");
        assertThat(Conformance.conformant()).isInstanceOf(Conformance.Conformant.class);
    }

    @Test
    void aNonConformantPdfNeedsAtLeastOneFinding() {
        assertThatThrownBy(() -> Conformance.notConformant(List.of())).hasMessageContaining("at least one finding");
        assertThatThrownBy(() -> Conformance.notConformant(null)).hasMessageContaining("findings");
    }
}
