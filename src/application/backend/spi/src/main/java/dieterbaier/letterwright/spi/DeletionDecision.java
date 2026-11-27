package dieterbaier.letterwright.spi;

/**
 * How a letter is deleted. The host determines the deletion and retention rules. A decision is created with
 * {@link #hard()}, {@link #logical()} or {@link #blocked(String)}.
 */
public sealed interface DeletionDecision {

    /** The letter and its PDF are removed. */
    static DeletionDecision hard() {
        return new Hard();
    }

    /** The letter is marked as deleted and no longer shown; the PDF is kept. */
    static DeletionDecision logical() {
        return new Logical();
    }

    /**
     * The letter must not be deleted, for example because of a retention period.
     *
     * @param reason why not; the library shows it to the user
     */
    static DeletionDecision blocked(String reason) {
        return new Blocked(reason);
    }

    /** See {@link #hard()}. */
    record Hard() implements DeletionDecision {
    }

    /** See {@link #logical()}. */
    record Logical() implements DeletionDecision {
    }

    /** See {@link #blocked(String)}. */
    record Blocked(String reason) implements DeletionDecision {

        public Blocked {
            Require.text(reason, "The reason for the deletion block");
        }
    }
}
