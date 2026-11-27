package dieterbaier.letterwright.domain.model.letter;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static dieterbaier.letterwright.domain.model.TestData.NOW;

import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.TestData;
import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LetterFromSnapshotTest {

    private final Snapshot snapshot = TestData.snapshot();
    private final DocumentReference document = new DocumentReference("store/1");
    private final Checksum pdf = Checksum.of("%PDF-1.7".getBytes(StandardCharsets.US_ASCII));

    @Test
    void requiredAttributesOfTheResult() {
        Letter letter = Letter.fromSnapshot(LetterId.generate(), snapshot, document, pdf, NOW).letter();

        rejects(() -> new LetterFromSnapshot(null, snapshot), "Das Schreiben");
        rejects(() -> new LetterFromSnapshot(letter, null), "verbrauchte Snapshot");
    }

    @Test
    void theResultIsConsistent() {
        LetterFromSnapshot created = Letter.fromSnapshot(LetterId.generate(), snapshot, document, pdf, NOW);
        Snapshot otherConsumed = Letter.fromSnapshot(LetterId.generate(), TestData.snapshot(), document, pdf, NOW)
                .consumedSnapshot();

        rejects(() -> new LetterFromSnapshot(created.letter(), snapshot), "nicht verbraucht");
        rejects(() -> new LetterFromSnapshot(created.letter(), otherConsumed), "entstand nicht aus Snapshot");
    }
}
