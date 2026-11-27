package dieterbaier.letterwright.spi;

/**
 * Anfrage an den {@link PlaceholderDataProvider}: Welche Werte braucht dieses Standardschreiben für diesen fachlichen
 * Bezug?
 *
 * @param userId der angemeldete Benutzer, wie ihn die Spring Security des Hosts kennt
 */
public record DataRequest(String userId, BusinessReference reference, LetterTemplateId letterTemplate) {

    public DataRequest {
        Require.text(userId, "Die Benutzerkennung");
        Require.value(reference, "Der fachliche Bezug");
        Require.value(letterTemplate, "Das Standardschreiben");
    }
}
