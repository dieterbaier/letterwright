package dieterbaier.letterwright.spi;

/**
 * Optional callback when a user saves a letter through the library's user interface (ADR-023). In it, the host changes
 * its own data in the same commit as the letter, or it rejects the save with a reason. The host implements this
 * interface as a bean if it needs it; without a bean, the library behaves as if there were no callback. There is at
 * most one callback. If the host provides several beans, Spring picks one by its usual rules, for example the one
 * annotated with {@code @Primary}; only that one is called. If the choice remains ambiguous, the application does not
 * start.
 *
 * <p><b>When:</b> The library calls {@link #created} synchronously, in the same transaction as the save, after its
 * own database changes and before the commit. There is no call after the commit. The host's database operations take
 * part in this transaction if the host uses the same transaction manager as the library.
 *
 * <p><b>Only on creation:</b> The callback runs only if the current call creates the letter. If the user saves the
 * same preview again, for example after a lost response, no new letter is created and there is no callback. The
 * contract guarantees nothing more: an existing letter may have been created through the Java API without a callback.
 * The Java API never triggers a callback; there, the host evaluates the result itself.
 *
 * <p><b>Rejecting:</b> {@link CallbackResult#rejected(String)} rolls back the shared transaction. Afterwards nothing
 * is saved, neither the letter nor the host's data, and the user sees the reason. The preview remains valid until it
 * expires; the user can save again.
 *
 * <p><b>Exceptions:</b> Every exception is a technical error and likewise rolls back the shared transaction. The
 * library does not retry; a retry is a new save attempt by the user.
 *
 * <p><b>Effects outside the database</b>, for example completing a task in a remote engine, do not belong in the
 * callback. For those, the host writes a job to its outbox in the same transaction and executes it itself after the
 * commit, retryably and idempotently.
 */
public interface LetterSaveCallback {

    /**
     * A user has created a letter through the user interface; the transaction is not yet committed.
     *
     * @param letter the stable identifiers of the new letter; the host links its data via the ID
     * @return {@link CallbackResult#accepted()} so that the letter is committed, or
     *     {@link CallbackResult#rejected(String)} with a reason for the user, which rolls back the transaction
     */
    CallbackResult created(SavedLetter letter);
}
