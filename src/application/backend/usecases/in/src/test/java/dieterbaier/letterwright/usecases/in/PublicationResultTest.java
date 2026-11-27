package dieterbaier.letterwright.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PublicationResultTest {

    private static final VersionReference VERSION = new VersionReference(TemplateId.generate(), VersionNumber.FIRST);

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void everyResultIsDistinguishable() {
        assertThat(Stream.of(new PublicationResult.Published(VERSION),
                new PublicationResult.NotConformant(List.of("Befund")),
                new PublicationResult.NotAllowed(), new PublicationResult.TemplateUnknown(),
                new PublicationResult.NoDraft(), new PublicationResult.DraftWithoutContent(),
                new PublicationResult.LetterheadNotPublished(),
                new PublicationResult.Stale())
                .map(result -> switch (result) {
                    case PublicationResult.Published published -> "veröffentlicht";
                    case PublicationResult.NotConformant notConformant -> "nicht konform";
                    case PublicationResult.NotAllowed notAllowed -> "nicht erlaubt";
                    case PublicationResult.TemplateUnknown unknown -> "unknown";
                    case PublicationResult.NoDraft noDraft -> "kein Entwurf";
                    case PublicationResult.DraftWithoutContent withoutContent -> "ohne Inhalt";
                    case PublicationResult.LetterheadNotPublished letterhead -> "Briefpapier fehlt";
                    case PublicationResult.Stale stale -> "stale";
                })).doesNotHaveDuplicates().hasSize(8);
    }

    @Test
    void publishedNamesTheVersion() {
        assertThat(new PublicationResult.Published(VERSION).version()).isEqualTo(VERSION);
        assertThatThrownBy(() -> new PublicationResult.Published(null))
                .hasMessageContaining("veröffentlichte Version");
    }

    @Test
    void notConformantNeedsAtLeastOneFindingAndCopiesTheList() {
        List<String> findings = new ArrayList<>(List.of("Bild ohne Alternativtext"));
        var notConformant = new PublicationResult.NotConformant(findings);
        findings.add("später");

        assertThat(notConformant.findings()).containsExactly("Bild ohne Alternativtext");
        assertThatThrownBy(() -> new PublicationResult.NotConformant(List.of()))
                .hasMessageContaining("mindestens einen Befund");
        assertThatThrownBy(() -> new PublicationResult.NotConformant(Arrays.asList("a", null)))
                .hasMessageContaining("Befunde");
    }
}
