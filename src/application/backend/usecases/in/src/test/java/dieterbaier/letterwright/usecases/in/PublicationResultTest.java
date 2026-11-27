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

    /** This is how an inbound adapter tells the results apart; the compiler checks that none is missing. */
    @Test
    void everyResultIsDistinguishable() {
        assertThat(Stream.of(new PublicationResult.Published(VERSION),
                new PublicationResult.NotConformant(List.of("Finding")),
                new PublicationResult.NotAllowed(), new PublicationResult.TemplateUnknown(),
                new PublicationResult.NoDraft(), new PublicationResult.DraftWithoutContent(),
                new PublicationResult.LetterheadNotPublished(),
                new PublicationResult.Stale())
                .map(result -> switch (result) {
                    case PublicationResult.Published published -> "published";
                    case PublicationResult.NotConformant notConformant -> "not conformant";
                    case PublicationResult.NotAllowed notAllowed -> "not allowed";
                    case PublicationResult.TemplateUnknown unknown -> "unknown";
                    case PublicationResult.NoDraft noDraft -> "no draft";
                    case PublicationResult.DraftWithoutContent withoutContent -> "without content";
                    case PublicationResult.LetterheadNotPublished letterhead -> "letterhead missing";
                    case PublicationResult.Stale stale -> "stale";
                })).doesNotHaveDuplicates().hasSize(8);
    }

    @Test
    void publishedNamesTheVersion() {
        assertThat(new PublicationResult.Published(VERSION).version()).isEqualTo(VERSION);
        assertThatThrownBy(() -> new PublicationResult.Published(null))
                .hasMessageContaining("published version");
    }

    @Test
    void notConformantNeedsAtLeastOneFindingAndCopiesTheList() {
        List<String> findings = new ArrayList<>(List.of("Image without alternative text"));
        var notConformant = new PublicationResult.NotConformant(findings);
        findings.add("later");

        assertThat(notConformant.findings()).containsExactly("Image without alternative text");
        assertThatThrownBy(() -> new PublicationResult.NotConformant(List.of()))
                .hasMessageContaining("at least one finding");
        assertThatThrownBy(() -> new PublicationResult.NotConformant(Arrays.asList("a", null)))
                .hasMessageContaining("findings");
    }
}
