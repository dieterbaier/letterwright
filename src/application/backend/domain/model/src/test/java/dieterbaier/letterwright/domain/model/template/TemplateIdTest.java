package dieterbaier.letterwright.domain.model.template;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TemplateIdTest {

    @Test
    void theTemplateIdRequiresAValueAndIsNewlyAssigned() {
        rejects(() -> new TemplateId(null), "TemplateId");
        assertThat(TemplateId.generate()).isNotEqualTo(TemplateId.generate());
    }
}
