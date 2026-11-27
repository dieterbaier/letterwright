package dieterbaier.letterwright.domain.model.letter;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static dieterbaier.letterwright.domain.model.TestData.LETTERHEAD_V1;
import static dieterbaier.letterwright.domain.model.TestData.NOW;
import static dieterbaier.letterwright.domain.model.TestData.CUSTOMER;
import static dieterbaier.letterwright.domain.model.TestData.LETTER_TEMPLATE_V3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.TestData;
import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotState;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LetterTest {

    private final Snapshot snapshot = TestData.snapshot();
    private final DocumentReference document = new DocumentReference("store/1");
    private final Checksum pdf = Checksum.of("%PDF-1.7".getBytes(StandardCharsets.US_ASCII));
    private static final LetterState VISIBLE = new LetterState.Visible();

    @Test
    void takesReferenceAndTemplateVersionsFromTheSnapshot() {
        LetterId id = LetterId.generate();
        Instant saved = NOW.plus(Duration.ofMinutes(5));

        LetterFromSnapshot created = Letter.fromSnapshot(id, snapshot, document, pdf, saved);
        Letter letter = created.letter();

        assertThat(letter.id()).isEqualTo(id);
        assertThat(letter.snapshot()).isEqualTo(snapshot.id());
        assertThat(letter.reference()).isEqualTo(CUSTOMER);
        assertThat(letter.letterhead()).isEqualTo(LETTERHEAD_V1);
        assertThat(letter.letterTemplate()).isEqualTo(LETTER_TEMPLATE_V3);
        assertThat(letter.document()).isEqualTo(document);
        assertThat(letter.pdfChecksum()).isEqualTo(pdf);
        assertThat(letter.created()).isEqualTo(saved);
        assertThat(letter.isVisible()).as("a new letter is visible").isTrue();
        assertThat(created.consumedSnapshot()).isEqualTo(snapshot);
        assertThat(created.consumedSnapshot().state()).isInstanceOf(SnapshotState.Consumed.class);
    }

    @Test
    void aConsumedSnapshotYieldsNoFurtherLetter() {
        Snapshot consumed = Letter.fromSnapshot(LetterId.generate(), snapshot, document, pdf, NOW)
                .consumedSnapshot();

        assertThatIllegalStateException()
                .isThrownBy(() -> Letter.fromSnapshot(LetterId.generate(), consumed, document, pdf, NOW))
                .withMessageContaining("already created");
    }

    @Test
    void anExpiredSnapshotYieldsNoLetter() {
        assertThatIllegalStateException()
                .isThrownBy(() -> Letter.fromSnapshot(LetterId.generate(), snapshot, document, pdf, snapshot.expiry()))
                .withMessageContaining("expired");
    }

    @Test
    void requiredAttributes() {
        LetterId id = LetterId.generate();

        rejects(() -> Letter.fromSnapshot(id, null, document, pdf, NOW), "snapshot");
        rejects(() -> Letter.fromSnapshot(id, snapshot, document, pdf, null), "time");
        rejects(() -> Letter.fromSnapshot(null, snapshot, document, pdf, NOW), "ID of the letter");
        rejects(() -> Letter.fromSnapshot(id, snapshot, null, pdf, NOW), "document reference of the letter");
        rejects(() -> Letter.fromSnapshot(id, snapshot, document, null, NOW), "checksum of the PDF");
        rejects(() -> Letter.restore(id, null, CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3, document,
                pdf, NOW, VISIBLE), "snapshot of the letter");
        rejects(() -> Letter.restore(id, snapshot.id(), null, LETTERHEAD_V1, LETTER_TEMPLATE_V3,
                document, pdf, NOW, VISIBLE), "business reference of the letter");
        rejects(() -> Letter.restore(id, snapshot.id(), CUSTOMER, null, LETTER_TEMPLATE_V3, document, pdf,
                NOW, VISIBLE), "letterhead version");
        rejects(() -> Letter.restore(id, snapshot.id(), CUSTOMER, LETTERHEAD_V1, null, document, pdf, NOW,
                VISIBLE), "letter template version");
        rejects(() -> Letter.restore(id, snapshot.id(), CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3,
                document, pdf, null, VISIBLE), "creation time");
        rejects(() -> Letter.restore(id, snapshot.id(), CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3,
                document, pdf, NOW, null), "state of the letter");
    }

    @Test
    void equalityByIdentity() {
        LetterId id = LetterId.generate();
        Letter letter = Letter.fromSnapshot(id, snapshot, document, pdf, NOW).letter();
        Letter loaded = Letter.restore(id, snapshot.id(), CUSTOMER, LETTERHEAD_V1, LETTER_TEMPLATE_V3,
                new DocumentReference("store/2"), pdf, NOW, VISIBLE);

        assertThat(loaded).isEqualTo(letter).hasSameHashCodeAs(letter);
        assertThat(Letter.fromSnapshot(LetterId.generate(), snapshot, document, pdf, NOW).letter())
                .isNotEqualTo(letter);
        assertThat(letter).isNotEqualTo(id);
        assertThat(letter.toString()).contains(id.value().toString());
    }

    @Test
    void aLogicallyDeletedLetterKeepsEverythingButItsVisibility() {
        Letter letter = Letter.fromSnapshot(LetterId.generate(), snapshot, document, pdf, NOW).letter();
        Instant deleted = NOW.plus(Duration.ofDays(1));

        Letter result = letter.logicallyDeleted(deleted);

        assertThat(result.isVisible()).isFalse();
        assertThat(result.state()).isEqualTo(new LetterState.LogicallyDeleted(deleted));
        assertThat(result).isEqualTo(letter);
        assertThat(result.document()).isEqualTo(document);
        assertThat(result.pdfChecksum()).isEqualTo(pdf);
        assertThat(result.created()).isEqualTo(NOW);
        assertThat(letter.isVisible()).as("the original stays unchanged").isTrue();
    }

    @Test
    void aLogicallyDeletedLetterIsNotDeletedAgain() {
        Letter deleted = Letter.fromSnapshot(LetterId.generate(), snapshot, document, pdf, NOW).letter()
                .logicallyDeleted(NOW);

        assertThatIllegalStateException().isThrownBy(() -> deleted.logicallyDeleted(NOW))
                .withMessageContaining("already logically deleted");
        rejects(() -> Letter.fromSnapshot(LetterId.generate(), TestData.snapshot(), document, pdf, NOW).letter()
                .logicallyDeleted(null), "time");
    }
}
