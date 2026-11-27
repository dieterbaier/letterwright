package dieterbaier.letterwright.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.letter.DocumentReference;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.domain.model.letter.LetterState;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SaveResultTest {

    private final Letter letter = Letter.restore(LetterId.generate(), SnapshotId.generate(),
            new BusinessReference("Customer", "4711"), new VersionReference(TemplateId.generate(), VersionNumber.FIRST),
            new VersionReference(TemplateId.generate(), VersionNumber.FIRST), new DocumentReference("store/1"),
            Checksum.of(new byte[] {1}), Instant.parse("2026-11-24T10:00:00Z"), new LetterState.Visible());

    /** This is how an inbound adapter tells the results apart; the compiler checks that none is missing. */
    @Test
    void everyResultIsDistinguishable() {
        assertThat(Stream.of(new SaveResult.Saved(letter),
                new SaveResult.AlreadySaved(letter), new SaveResult.SnapshotUnknown(),
                new SaveResult.SnapshotExpired(), new SaveResult.NotAllowed(),
                new SaveResult.DataContextChanged(), new SaveResult.NotConformant(List.of("Finding")),
                new SaveResult.RejectedByHost("Task is already done"))
                .map(SaveResultTest::status))
                .containsExactly(201, 200, 404, 410, 403, 409, 422, 400);
    }

    private static int status(SaveResult result) {
        return switch (result) {
            case SaveResult.Saved saved -> 201;
            case SaveResult.AlreadySaved already -> 200;
            case SaveResult.SnapshotUnknown unknown -> 404;
            case SaveResult.SnapshotExpired expired -> 410;
            case SaveResult.NotAllowed notAllowed -> 403;
            case SaveResult.DataContextChanged changed -> 409;
            case SaveResult.NotConformant notConformant -> 422;
            case SaveResult.RejectedByHost rejected -> 400;
        };
    }

    @Test
    void requiredAttributes() {
        assertThat(new SaveResult.Saved(letter).letter()).isEqualTo(letter);
        assertThat(new SaveResult.AlreadySaved(letter).letter()).isEqualTo(letter);
        assertThat(new SaveResult.NotConformant(List.of("Finding")).findings()).containsExactly("Finding");
        assertThatThrownBy(() -> new SaveResult.Saved(null)).hasMessageContaining("saved");
        assertThatThrownBy(() -> new SaveResult.AlreadySaved(null)).hasMessageContaining("already");
        assertThatThrownBy(() -> new SaveResult.NotConformant(null)).hasMessageContaining("findings");
        assertThat(new SaveResult.RejectedByHost("Task is already done").reason())
                .isEqualTo("Task is already done");
        assertThatThrownBy(() -> new SaveResult.RejectedByHost(" ")).hasMessageContaining("reason");
    }
}
