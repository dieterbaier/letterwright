package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotState;
import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * A new letter and the snapshot it consumed. Both are saved together: the letter only counts if the stored snapshot
 * is still open at that moment (ADR-021).
 */
@ValueObject
public record LetterFromSnapshot(Letter letter, Snapshot consumedSnapshot) {

    public LetterFromSnapshot {
        Validate.required(letter, "The letter");
        Validate.required(consumedSnapshot, "The consumed snapshot");
        if (!(consumedSnapshot.state() instanceof SnapshotState.Consumed)) {
            throw new ValidationException("The snapshot " + consumedSnapshot.id().value() + " is not consumed");
        }
        if (!letter.snapshot().equals(consumedSnapshot.id())) {
            throw new ValidationException("The letter " + letter.id().value() + " was not created from snapshot "
                    + consumedSnapshot.id().value());
        }
    }
}
