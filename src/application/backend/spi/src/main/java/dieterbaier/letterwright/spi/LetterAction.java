package dieterbaier.letterwright.spi;

/**
 * An action on the saved letters for a {@link BusinessReference}. Creating a letter is not a {@code LetterAction}: it
 * also needs the letter template, see
 * {@link AuthorizationRequest#forCreation(String, BusinessReference, LetterTemplateId)}.
 */
public enum LetterAction {

    /** See the letters in the list. */
    VIEW,

    /** Download a letter. */
    DOWNLOAD,

    /** Delete a letter. The {@link DeletionPolicy} decides how. */
    DELETE
}
