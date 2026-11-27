package dieterbaier.letterwright.api;

import java.util.UUID;

/**
 * Use case "save letter" for the host's code (ADR-023). The host calls it from its own use case, evaluates the result
 * and commits its own database changes together with the letter.
 *
 * <p><b>Delivery status:</b> The starter exports this interface but does not yet provide an implementation as a bean.
 * That comes with the wiring of the use cases in the starter (issue #10). Until then, a host can compile against the
 * interface but cannot inject it.
 *
 * <p>The same rules apply as when saving through the user interface: the same authorization check, the same PDF from
 * the snapshot of the preview, saved only if it is conformant, and at most one letter per snapshot (ADR-021). This path
 * makes no callback to the host.
 *
 * <p><b>Transaction:</b> The call joins a running transaction and opens its own only if none is running (Spring
 * {@code REQUIRED}); it never opens an independent transaction. Saving is therefore atomic with the host's own
 * operations only if the host opens the transaction before the call, using the same transaction manager as the
 * library. If the host rolls back, the letter and the consumption of the snapshot are rolled back with it; the stored
 * PDF then remains without a letter.
 *
 * <p>A business rejection is a case of {@link SaveResult} and does not mark the transaction for rollback. Any exception
 * from this call, however, marks a running transaction for rollback, even if the host catches it: a technical error
 * as well as an {@link IllegalArgumentException} for invalid input, that is, an error of the caller.
 */
public interface SaveLetter {

    /**
     * Saves the letter from the snapshot of a preview.
     *
     * @param userId the signed-in user as the host's Spring Security knows them; the same user who created the
     *     preview, otherwise the snapshot is unknown. Not empty and not only whitespace
     * @param snapshot the ID of the snapshot the host received with the preview
     * @return the result; on a rejection nothing is saved
     */
    SaveResult save(String userId, UUID snapshot);
}
