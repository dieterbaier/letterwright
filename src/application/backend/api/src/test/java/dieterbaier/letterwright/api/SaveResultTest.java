package dieterbaier.letterwright.api;

import static dieterbaier.letterwright.api.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SaveResultTest {

    private final SavedLetter letter = new SavedLetter(UUID.randomUUID(),
            new BusinessReference("Customer", "4711"), new LetterTemplateVersion(UUID.randomUUID(), 1), "erika",
            Instant.parse("2026-11-24T10:00:00Z"));

    /** This is how a host distinguishes the results; the compiler checks that none is missing. */
    @Test
    void everyResultIsDistinguishable() {
        assertThat(Stream.of(new SaveResult.Saved(letter),
                new SaveResult.AlreadySaved(letter), new SaveResult.SnapshotUnknown(),
                new SaveResult.SnapshotExpired(), new SaveResult.NotAllowed(),
                new SaveResult.DataContextChanged(), new SaveResult.NotConformant(List.of("Finding")))
                .map(SaveResultTest::name))
                .containsExactly("saved", "already", "unknown", "expired", "not allowed", "changed",
                        "not conformant");
    }

    private static String name(SaveResult result) {
        return switch (result) {
            case SaveResult.Saved saved -> "saved";
            case SaveResult.AlreadySaved already -> "already";
            case SaveResult.SnapshotUnknown unknown -> "unknown";
            case SaveResult.SnapshotExpired expired -> "expired";
            case SaveResult.NotAllowed notAllowed -> "not allowed";
            case SaveResult.DataContextChanged changed -> "changed";
            case SaveResult.NotConformant notConformant -> "not conformant";
        };
    }

    @Test
    void requiredAttributes() {
        assertThat(new SaveResult.Saved(letter).letter()).isEqualTo(letter);
        assertThat(new SaveResult.AlreadySaved(letter).letter()).isEqualTo(letter);
        rejects(() -> new SaveResult.Saved(null), "The saved letter");
        rejects(() -> new SaveResult.AlreadySaved(null), "The already saved letter");
    }

    @Test
    void theFindingsAreAnImmutableCopyWithoutGaps() {
        List<String> findings = new ArrayList<>(List.of("PDF/UA-1: Image without alternative text"));
        var notConformant = new SaveResult.NotConformant(findings);
        findings.clear();

        assertThat(notConformant.findings()).containsExactly("PDF/UA-1: Image without alternative text");
        assertThatThrownBy(() -> notConformant.findings().add("another"))
                .isInstanceOf(UnsupportedOperationException.class);
        rejects(() -> new SaveResult.NotConformant(null), "The findings");
        rejects(() -> new SaveResult.NotConformant(Arrays.asList("Finding", null)), "The findings");
    }
}
