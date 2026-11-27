package dieterbaier.letterwright.usecases.in;

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

/** Ein gespeichertes Schreiben für die Tests der Ergebnistypen. */
final class TestLetters {

    /** Nur lesen; wer es ändern will, nimmt eine Kopie. */
    static final byte[] PDF = {1, 2, 3};
    static final Letter LETTER = Letter.restore(LetterId.generate(), SnapshotId.generate(),
            new BusinessReference("Kunde", "4711"), new VersionReference(TemplateId.generate(), VersionNumber.FIRST),
            new VersionReference(TemplateId.generate(), VersionNumber.FIRST), new DocumentReference("store/1"),
            Checksum.of(PDF), Instant.parse("2026-11-22T10:00:00Z"), new LetterState.Visible());

    private TestLetters() {
    }
}
