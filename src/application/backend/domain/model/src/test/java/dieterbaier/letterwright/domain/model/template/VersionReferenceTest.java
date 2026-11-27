package dieterbaier.letterwright.domain.model.template;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VersionReferenceTest {

    @Test
    void versionReferenceRequiresTemplateAndNumber() {
        rejects(() -> new VersionReference(null, VersionNumber.FIRST), "Vorlage des Versionsverweises");
        rejects(() -> new VersionReference(TemplateId.generate(), null), "Nummer des Versionsverweises");
    }

    @Test
    void versionReferencesAreValueEqual() {
        TemplateId template = TemplateId.generate();

        assertThat(new VersionReference(template, VersionNumber.FIRST))
                .isEqualTo(new VersionReference(template, new VersionNumber(1)));
    }
}
