package dieterbaier.letterwright.spi;

import java.time.Instant;
import java.util.UUID;

/**
 * The stable identifiers of a saved letter that the {@link LetterSaveCallback callback} receives (ADR-023). The host
 * links its own data to the letter via the ID.
 *
 * @param id the ID of the letter, a technical key that never changes
 * @param reference the business reference the letter was created for
 * @param letterTemplate the version of the letter template it was created from
 * @param userId the user who created and saved the preview, as the host's Spring Security knows them
 * @param created the time of creation
 */
public record SavedLetter(
        UUID id, BusinessReference reference, LetterTemplateVersion letterTemplate, String userId,
        Instant created) {

    public SavedLetter {
        Require.value(id, "The ID of the letter");
        Require.value(reference, "The business reference");
        Require.value(letterTemplate, "The letter template");
        Require.text(userId, "The user ID");
        Require.value(created, "The time of creation");
    }
}
