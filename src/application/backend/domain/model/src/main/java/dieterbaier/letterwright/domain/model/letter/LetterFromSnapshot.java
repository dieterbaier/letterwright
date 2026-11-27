package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotState;
import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Ein neues Schreiben und der Snapshot, den es verbraucht hat. Beide werden zusammen gespeichert: Nur wenn der
 * gespeicherte Snapshot dabei noch offen ist, gilt das Schreiben (ADR-021).
 */
@ValueObject
public record LetterFromSnapshot(Letter letter, Snapshot consumedSnapshot) {

    public LetterFromSnapshot {
        Validate.required(letter, "Das Schreiben");
        Validate.required(consumedSnapshot, "Der verbrauchte Snapshot");
        if (!(consumedSnapshot.state() instanceof SnapshotState.Consumed)) {
            throw new ValidationException("Der Snapshot " + consumedSnapshot.id().value() + " ist nicht verbraucht");
        }
        if (!letter.snapshot().equals(consumedSnapshot.id())) {
            throw new ValidationException("Das Schreiben " + letter.id().value() + " entstand nicht aus Snapshot "
                    + consumedSnapshot.id().value());
        }
    }
}
