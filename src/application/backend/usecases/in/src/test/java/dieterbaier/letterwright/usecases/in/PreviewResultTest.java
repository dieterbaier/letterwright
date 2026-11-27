package dieterbaier.letterwright.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PreviewResultTest {

    private final SnapshotId snapshot = SnapshotId.generate();
    private final Instant expiry = Instant.parse("2026-11-22T10:30:00Z");

    @Test
    void aCreatedPreviewHasSnapshotHtmlAndExpiry() {
        var created = new PreviewResult.Created(snapshot, "<p/>", expiry);

        assertThat(created.snapshot()).isEqualTo(snapshot);
        assertThat(created.html()).isEqualTo("<p/>");
        assertThat(created.expiry()).isEqualTo(expiry);
    }

    /** This is how an inbound adapter tells the results apart; the compiler checks that none is missing. */
    @Test
    void everyResultIsDistinguishable() {
        assertThat(java.util.stream.Stream.of(new PreviewResult.Created(snapshot, "<p/>", expiry),
                new PreviewResult.LetterTemplateUnknown(), new PreviewResult.ReferenceMismatch(),
                new PreviewResult.NoPublishedVersion(), new PreviewResult.NotAllowed())
                .map(PreviewResultTest::status))
                .containsExactly(200, 404, 422, 409, 403);
    }

    private static int status(PreviewResult result) {
        return switch (result) {
            case PreviewResult.Created created -> 200;
            case PreviewResult.LetterTemplateUnknown unknown -> 404;
            case PreviewResult.ReferenceMismatch doesNotMatch -> 422;
            case PreviewResult.NoPublishedVersion noVersion -> 409;
            case PreviewResult.NotAllowed notAllowed -> 403;
        };
    }

    @Test
    void requiredAttributes() {
        assertThatThrownBy(() -> new PreviewResult.Created(null, "<p/>", expiry)).hasMessageContaining("snapshot");
        assertThatThrownBy(() -> new PreviewResult.Created(snapshot, null, expiry)).hasMessageContaining("HTML");
        assertThatThrownBy(() -> new PreviewResult.Created(snapshot, "<p/>", null)).hasMessageContaining("expiry");
    }
}
