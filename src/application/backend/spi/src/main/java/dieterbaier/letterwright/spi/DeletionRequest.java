package dieterbaier.letterwright.spi;

import java.time.Instant;
import java.util.UUID;

/**
 * A request to the {@link DeletionPolicy}: a user wants to delete this letter.
 *
 * @param userId the signed-in user as the host's Spring Security knows them
 * @param letter the ID of the letter
 * @param created when the letter was saved, for example for retention periods
 */
public record DeletionRequest(String userId, UUID letter, BusinessReference reference, Instant created) {

    public DeletionRequest {
        Require.text(userId, "The user ID");
        Require.value(letter, "The ID of the letter");
        Require.value(reference, "The business reference");
        Require.value(created, "The creation time");
    }
}
