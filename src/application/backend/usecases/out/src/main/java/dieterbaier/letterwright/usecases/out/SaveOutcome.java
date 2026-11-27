package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.validation.Validate;

/** A certain outcome of {@link LetterRepository#save}. An uncertain outcome is an exception. */
public sealed interface SaveOutcome {

    record Saved() implements SaveOutcome {}

    /** Nothing was saved; the snapshot no longer exists, for example because a new preview replaced it. */
    record SnapshotNoLongerExists() implements SaveOutcome {}

    /**
     * Nothing was saved; this letter has already been created from the snapshot. It may since have been logically
     * deleted.
     */
    record SnapshotAlreadyConsumed(Letter existing) implements SaveOutcome {

        public SnapshotAlreadyConsumed {
            Validate.required(existing, "The existing letter");
        }
    }

    /** Nothing was saved; a letter has already been created from the snapshot, and it is hard deleted. */
    record LetterRemoved() implements SaveOutcome {}
}
