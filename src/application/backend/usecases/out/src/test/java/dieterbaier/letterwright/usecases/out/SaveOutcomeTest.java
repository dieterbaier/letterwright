package dieterbaier.letterwright.usecases.out;

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
import org.junit.jupiter.api.Test;

class SaveOutcomeTest {

    private final Letter existing = Letter.restore(LetterId.generate(), SnapshotId.generate(),
            new BusinessReference("Customer", "4711"), new VersionReference(TemplateId.generate(), VersionNumber.FIRST),
            new VersionReference(TemplateId.generate(), VersionNumber.FIRST), new DocumentReference("store/1"),
            Checksum.of(new byte[] {1}), Instant.parse("2026-11-22T10:00:00Z"),
            new LetterState.Visible());

    @Test
    void aConsumedSnapshotNamesTheExistingLetter() {
        assertThat(new SaveOutcome.SnapshotAlreadyConsumed(existing).existing()).isEqualTo(existing);
        assertThat(new SaveOutcome.Saved()).isInstanceOf(SaveOutcome.class);
        assertThat(new SaveOutcome.SnapshotNoLongerExists()).isInstanceOf(SaveOutcome.class);
        assertThat(new SaveOutcome.LetterRemoved()).isInstanceOf(SaveOutcome.class);
        assertThatThrownBy(() -> new SaveOutcome.SnapshotAlreadyConsumed(null))
                .hasMessageContaining("existing letter");
    }
}
