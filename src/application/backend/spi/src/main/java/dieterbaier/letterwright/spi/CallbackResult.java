package dieterbaier.letterwright.spi;

/**
 * How the host answers a newly created letter, see {@link LetterSaveCallback}. A result is created with
 * {@link #accepted()} or {@link #rejected(String)}.
 */
public sealed interface CallbackResult {

    /** The host agrees; the letter and the host's data are committed together. */
    static CallbackResult accepted() {
        return new Accepted();
    }

    /**
     * The host rejects the save; the shared transaction is rolled back.
     *
     * @param reason why not; the library shows it to the user. Not empty and not only whitespace
     */
    static CallbackResult rejected(String reason) {
        return new Rejected(reason);
    }

    /** See {@link #accepted()}. */
    record Accepted() implements CallbackResult {
    }

    /** See {@link #rejected(String)}. */
    record Rejected(String reason) implements CallbackResult {

        public Rejected {
            Require.text(reason, "The reason for the rejection");
        }
    }
}
