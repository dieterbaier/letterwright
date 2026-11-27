package dieterbaier.letterwright.usecases.service;

import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.domain.model.letter.LetterState;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** Stores letters with their PDF in the fakes, as if they had been saved earlier. */
final class SavedLetters {

    private final Fakes.LetterStore letters;
    private final Fakes.Documents documents;
    private final VersionReference letterhead = new VersionReference(TemplateId.generate(), VersionNumber.FIRST);
    private final VersionReference reminder = new VersionReference(TemplateId.generate(), VersionNumber.FIRST);

    SavedLetters(Fakes.LetterStore letters, Fakes.Documents documents) {
        this.letters = letters;
        this.documents = documents;
    }

    Letter save(BusinessReference reference, Instant created) {
        byte[] pdf = ("%PDF " + reference.id() + " " + created).getBytes(StandardCharsets.UTF_8);
        return letters.store(Letter.restore(LetterId.generate(), SnapshotId.generate(), reference, letterhead,
                reminder, documents.put(pdf), Checksum.of(pdf), created, new LetterState.Visible()));
    }

    Letter deleteLogically(Letter existing, Instant at) {
        Letter deleted = existing.logicallyDeleted(at);
        letters.replace(deleted);
        return deleted;
    }
}
