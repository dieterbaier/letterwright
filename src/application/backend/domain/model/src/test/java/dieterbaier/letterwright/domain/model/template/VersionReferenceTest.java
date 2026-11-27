package dieterbaier.letterwright.domain.model.template;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VersionReferenceTest {

    @Test
    void versionReferenceRequiresTemplateAndNumber() {
        rejects(() -> new VersionReference(null, VersionNumber.FIRST), "template of the VersionReference");
        rejects(() -> new VersionReference(TemplateId.generate(), null), "number of the VersionReference");
    }

    @Test
    void versionReferencesAreValueEqual() {
        TemplateId template = TemplateId.generate();

        assertThat(new VersionReference(template, VersionNumber.FIRST))
                .isEqualTo(new VersionReference(template, new VersionNumber(1)));
    }
}
