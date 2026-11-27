package dieterbaier.letterwright.spi;

/**
 * A request to the {@link PlaceholderDataProvider}: which values does this letter template need for this business
 * reference?
 *
 * @param userId the signed-in user as the host's Spring Security knows them
 */
public record DataRequest(String userId, BusinessReference reference, LetterTemplateId letterTemplate) {

    public DataRequest {
        Require.text(userId, "The user ID");
        Require.value(reference, "The business reference");
        Require.value(letterTemplate, "The letter template");
    }
}
