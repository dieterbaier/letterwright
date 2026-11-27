package dieterbaier.letterwright.domain.model.template;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** {@link LetterheadTest} checks the version rules themselves; this is about what makes the letter template. */
class LetterTemplateTest {

    private final TemplateId id = TemplateId.generate();
    private final TemplateId letterhead = TemplateId.generate();

    @Test
    void knowsExactlyOneLetterheadAndRunsThroughTheVersions() {
        LetterTemplate created =
                LetterTemplate.create(id, "Payment reminder", letterhead, "Customer", "<p>Payment reminder</p>");

        LetterTemplate withV2 = created.changeDraft("<p>1st payment reminder</p>").publishDraft()
                .newDraft("<p>2</p>").changeDraft("<p>2nd payment reminder</p>");
        LetterTemplate withNewV2 = withV2.publishDraft();

        assertThat(withV2.letterhead()).isEqualTo(letterhead);
        assertThat(withV2.name()).isEqualTo("Payment reminder");
        assertThat(withV2.id()).isEqualTo(id);
        assertThat(withV2.versions()).hasSize(2);
        assertThat(withV2.draft()).get().extracting(TemplateVersion::content).isEqualTo("<p>2nd payment reminder</p>");
        assertThat(withV2.version(VersionNumber.FIRST)).get().extracting(TemplateVersion::content)
                .isEqualTo("<p>1st payment reminder</p>");
        assertThat(withNewV2.publishedVersion()).get().extracting(TemplateVersion::number)
                .isEqualTo(new VersionNumber(2));
    }

    @Test
    void restore() {
        LetterTemplate loaded = LetterTemplate.restore(id, "Payment reminder", letterhead, "Customer", List.of(
                TemplateVersion.restore(id, VersionNumber.FIRST, TemplateVersionState.PUBLISHED, "x",
                        DataContext.from(Map.of()))));

        assertThat(loaded.publishedVersion()).isPresent();
        rejects(() -> LetterTemplate.restore(id, "Payment reminder", letterhead, "Customer", List.of()),
                "at least one version");
    }

    @Test
    void requiredAttributes() {
        rejects(() -> LetterTemplate.create(null, "M", letterhead, "Customer", ""), "ID of the letter template");
        rejects(() -> LetterTemplate.create(id, "", letterhead, "Customer", ""), "name of the letter template");
        rejects(() -> LetterTemplate.create(id, "M", null, "Customer", ""), "letterhead of the letter template");
        rejects(() -> LetterTemplate.create(id, "M", letterhead, " ", ""), "reference type of the letter template");
        rejects(() -> LetterTemplate.restore(id, "M", letterhead, null, List.of(
                TemplateVersion.restore(id, VersionNumber.FIRST, TemplateVersionState.DRAFT, "",
                        DataContext.from(Map.of())))),
                "reference type of the letter template");
        rejects(() -> LetterTemplate.restore(null, "M", letterhead, "Customer", List.of()),
                "ID of the letter template");
    }

    @Test
    void matchesOnlyAReferenceOfItsReferenceType() {
        LetterTemplate reminder = LetterTemplate.create(id, "Payment reminder", letterhead, "Customer", "");

        assertThat(reminder.referenceType()).isEqualTo("Customer");
        assertThat(reminder.matches(new BusinessReference("Customer", "4711"))).isTrue();
        assertThat(reminder.matches(new BusinessReference("Supplier", "4711"))).isFalse();
    }

    @Test
    void equalityByIdentity() {
        LetterTemplate created = LetterTemplate.create(id, "Payment reminder", letterhead, "Customer", "");

        assertThat(created.changeDraft("x")).isEqualTo(created).hasSameHashCodeAs(created);
        assertThat(LetterTemplate.create(TemplateId.generate(), "Payment reminder", letterhead, "Customer", ""))
                .isNotEqualTo(created);
        assertThat(created).isNotEqualTo(id);
        assertThat(created.toString()).contains("Payment reminder");
    }
}
