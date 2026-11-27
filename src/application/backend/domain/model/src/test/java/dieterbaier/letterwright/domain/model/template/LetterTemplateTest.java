package dieterbaier.letterwright.domain.model.template;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Die Versionsregeln selbst prüft {@link LetterheadTest}; hier geht es um das, was das Standardschreiben ausmacht. */
class LetterTemplateTest {

    private final TemplateId id = TemplateId.generate();
    private final TemplateId letterhead = TemplateId.generate();

    @Test
    void knowsExactlyOneLetterheadAndRunsThroughTheVersions() {
        LetterTemplate generate = LetterTemplate.create(id, "Mahnung", letterhead, "Kunde", "<p>Mahnung</p>");

        LetterTemplate withV2 = generate.changeDraft("<p>1. Mahnung</p>").publishDraft()
                .newDraft("<p>2</p>").changeDraft("<p>2. Mahnung</p>");
        LetterTemplate withNewV2 = withV2.publishDraft();

        assertThat(withV2.letterhead()).isEqualTo(letterhead);
        assertThat(withV2.name()).isEqualTo("Mahnung");
        assertThat(withV2.id()).isEqualTo(id);
        assertThat(withV2.versions()).hasSize(2);
        assertThat(withV2.draft()).get().extracting(TemplateVersion::content).isEqualTo("<p>2. Mahnung</p>");
        assertThat(withV2.version(VersionNumber.FIRST)).get().extracting(TemplateVersion::content)
                .isEqualTo("<p>1. Mahnung</p>");
        assertThat(withNewV2.publishedVersion()).get().extracting(TemplateVersion::number)
                .isEqualTo(new VersionNumber(2));
    }

    @Test
    void restore() {
        LetterTemplate loaded = LetterTemplate.restore(id, "Mahnung", letterhead, "Kunde", List.of(
                TemplateVersion.restore(id, VersionNumber.FIRST, TemplateVersionState.PUBLISHED, "x",
                        DataContext.from(Map.of()))));

        assertThat(loaded.publishedVersion()).isPresent();
        rejects(() -> LetterTemplate.restore(id, "Mahnung", letterhead, "Kunde", List.of()),
                "mindestens eine Version");
    }

    @Test
    void requiredAttributes() {
        rejects(() -> LetterTemplate.create(null, "M", letterhead, "Kunde", ""), "ID des Standardschreibens");
        rejects(() -> LetterTemplate.create(id, "", letterhead, "Kunde", ""), "Name des Standardschreibens");
        rejects(() -> LetterTemplate.create(id, "M", null, "Kunde", ""), "Briefpapier des Standardschreibens");
        rejects(() -> LetterTemplate.create(id, "M", letterhead, " ", ""), "Bezugstyp des Standardschreibens");
        rejects(() -> LetterTemplate.restore(id, "M", letterhead, null, List.of(
                TemplateVersion.restore(id, VersionNumber.FIRST, TemplateVersionState.DRAFT, "",
                        DataContext.from(Map.of())))),
                "Bezugstyp des Standardschreibens");
        rejects(() -> LetterTemplate.restore(null, "M", letterhead, "Kunde", List.of()),
                "ID des Standardschreibens");
    }

    @Test
    void matchesOnlyAReferenceOfItsReferenceType() {
        LetterTemplate reminder = LetterTemplate.create(id, "Mahnung", letterhead, "Kunde", "");

        assertThat(reminder.referenceType()).isEqualTo("Kunde");
        assertThat(reminder.matches(new BusinessReference("Kunde", "4711"))).isTrue();
        assertThat(reminder.matches(new BusinessReference("Lieferant", "4711"))).isFalse();
    }

    @Test
    void equalityByIdentity() {
        LetterTemplate generate = LetterTemplate.create(id, "Mahnung", letterhead, "Kunde", "");

        assertThat(generate.changeDraft("x")).isEqualTo(generate).hasSameHashCodeAs(generate);
        assertThat(LetterTemplate.create(TemplateId.generate(), "Mahnung", letterhead, "Kunde", "")).isNotEqualTo(generate);
        assertThat(generate).isNotEqualTo(id);
        assertThat(generate.toString()).contains("Mahnung");
    }
}
