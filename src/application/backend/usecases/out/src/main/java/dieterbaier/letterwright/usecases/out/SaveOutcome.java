package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.validation.Validate;

/** Sicherer Ausgang von {@link LetterRepository#save}. Ein ungewisser Ausgang ist eine Ausnahme. */
public sealed interface SaveOutcome {

    record Saved() implements SaveOutcome {}

    /** Gespeichert wurde nichts; den Snapshot gibt es nicht mehr, etwa weil eine neue Vorschau ihn ersetzt hat. */
    record SnapshotNoLongerExists() implements SaveOutcome {}

    /**
     * Gespeichert wurde nichts; aus dem Snapshot ist schon dieses Schreiben entstanden. Es kann inzwischen logisch
     * gelöscht sein.
     */
    record SnapshotAlreadyConsumed(Letter existing) implements SaveOutcome {

        public SnapshotAlreadyConsumed {
            Validate.required(existing, "Das vorhandene Schreiben");
        }
    }

    /** Gespeichert wurde nichts; aus dem Snapshot ist schon ein Schreiben entstanden, und es ist hart gelöscht. */
    record LetterRemoved() implements SaveOutcome {}
}
