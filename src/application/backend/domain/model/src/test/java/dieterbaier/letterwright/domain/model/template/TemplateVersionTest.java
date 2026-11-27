package dieterbaier.letterwright.domain.model.template;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.DataContext;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TemplateVersionTest {

    private static final DataContext EMPTY = DataContext.from(Map.of());

    private final TemplateId id = TemplateId.generate();

    private static TemplateVersion version(TemplateId template, int number, TemplateVersionState state) {
        return TemplateVersion.restore(template, new VersionNumber(number), state, "<p>" + number + "</p>", EMPTY);
    }

    @Test
    void requiredAttributesOfTheVersion() {
        VersionNumber one = VersionNumber.FIRST;
        TemplateVersionState draft = TemplateVersionState.DRAFT;

        rejects(() -> TemplateVersion.restore(null, one, draft, "", EMPTY), "Vorlage der Version");
        rejects(() -> TemplateVersion.restore(id, null, draft, "", EMPTY), "VersionNumber");
        rejects(() -> TemplateVersion.restore(id, one, null, "", EMPTY), "Zustand der Version");
        rejects(() -> TemplateVersion.restore(id, one, draft, null, EMPTY), "Inhalt der Version");
        rejects(() -> TemplateVersion.restore(id, one, draft, "", null), "Beispieldaten der Version");
        for (TemplateVersionState released : new TemplateVersionState[] {TemplateVersionState.PUBLISHED,
                TemplateVersionState.SUPERSEDED, TemplateVersionState.WITHDRAWN}) {
            rejects(() -> TemplateVersion.restore(id, one, released, " ", EMPTY),
                    "Inhalt einer freigegebenen");
        }
    }

    @Test
    void onlyADraftWithContentIsPublishable() {
        assertThat(version(id, 1, TemplateVersionState.DRAFT).isPublishable()).isTrue();
        assertThat(TemplateVersion.restore(id, VersionNumber.FIRST, TemplateVersionState.DRAFT, " \n", EMPTY)
                .isPublishable()).as("nur Leerraum").isFalse();
        assertThat(version(id, 1, TemplateVersionState.PUBLISHED).isPublishable()).isFalse();
        assertThat(version(id, 1, TemplateVersionState.SUPERSEDED).isPublishable()).isFalse();
    }

    @Test
    void equalityOfVersionsByTemplateAndNumber() {
        TemplateVersion draft = version(id, 1, TemplateVersionState.DRAFT);
        TemplateVersion published = version(id, 1, TemplateVersionState.PUBLISHED);

        assertThat(published).isEqualTo(draft).hasSameHashCodeAs(draft);
        assertThat(version(id, 2, TemplateVersionState.DRAFT)).isNotEqualTo(draft);
        assertThat(version(TemplateId.generate(), 1, TemplateVersionState.DRAFT)).isNotEqualTo(draft);
        assertThat(draft).isNotEqualTo(draft.reference());
        assertThat(draft.toString()).contains("nummer=1", "DRAFT").doesNotContain("<p>");
    }
}
