package dieterbaier.letterwright.spi;

/**
 * A request to the {@link AuthorizationCheck}. There are exactly three kinds, and a host distinguishes them with
 * {@code switch}:
 *
 * <pre>{@code
 * return switch (request) {
 *     case AuthorizationRequest.ForTemplates templates -> ...;
 *     case AuthorizationRequest.ForLetter letter -> ... letter.reference() ...;
 *     case AuthorizationRequest.ForCreation create -> ... create.reference(), create.letterTemplate() ...;
 * };
 * }</pre>
 *
 * <p>When a letter is created, the host knows the letter template. This lets it decide per entry point which letter
 * templates are allowed for which reference. The library has already checked beforehand that the reference type
 * matches the letter template.
 */
public sealed interface AuthorizationRequest {

    /** The signed-in user as the host's Spring Security knows them. */
    String userId();

    /** May the user perform this action on letterheads and letter templates? */
    static AuthorizationRequest forTemplates(String userId, TemplateAction action) {
        return new ForTemplates(userId, action);
    }

    /** May the user perform this action on the saved letters for this business reference? */
    static AuthorizationRequest forLetter(String userId, LetterAction action, BusinessReference reference) {
        return new ForLetter(userId, action, reference);
    }

    /**
     * May the user create a letter from this letter template for this business reference? The library asks when the
     * preview is created and again when the letter is saved.
     */
    static AuthorizationRequest forCreation(
            String userId, BusinessReference reference, LetterTemplateId letterTemplate) {
        return new ForCreation(userId, reference, letterTemplate);
    }

    /** See {@link #forTemplates(String, TemplateAction)}. */
    record ForTemplates(String userId, TemplateAction action) implements AuthorizationRequest {

        public ForTemplates {
            Require.text(userId, "The user ID");
            Require.value(action, "The action");
        }
    }

    /** See {@link #forLetter(String, LetterAction, BusinessReference)}. */
    record ForLetter(String userId, LetterAction action, BusinessReference reference)
            implements AuthorizationRequest {

        public ForLetter {
            Require.text(userId, "The user ID");
            Require.value(action, "The action");
            Require.value(reference, "The business reference");
        }
    }

    /** See {@link #forCreation(String, BusinessReference, LetterTemplateId)}. */
    record ForCreation(String userId, BusinessReference reference, LetterTemplateId letterTemplate)
            implements AuthorizationRequest {

        public ForCreation {
            Require.text(userId, "The user ID");
            Require.value(reference, "The business reference");
            Require.value(letterTemplate, "The letter template");
        }
    }
}
