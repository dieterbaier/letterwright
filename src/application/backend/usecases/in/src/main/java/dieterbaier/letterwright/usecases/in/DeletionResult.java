package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.validation.Validate;

/** The outcome of deleting a letter. */
public sealed interface DeletionResult {

    /** The letter is gone for the user; the deletion policy decided whether it was hard or logically deleted. */
    record Deleted() implements DeletionResult {}

    /** The host's deletion policy blocks the deletion; the reason is meant for the user. */
    record Blocked(String reason) implements DeletionResult {

        public Blocked {
            Validate.notBlank(reason, "The reason for the deletion block");
        }
    }

    /**
     * The letter does not exist, is already logically deleted, or the user may not delete it. The cases are deliberately
     * indistinguishable (QS-009).
     */
    record Unknown() implements DeletionResult {}
}
