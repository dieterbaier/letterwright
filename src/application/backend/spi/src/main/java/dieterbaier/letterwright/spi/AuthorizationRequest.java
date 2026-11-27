package dieterbaier.letterwright.spi;

/**
 * Anfrage an die {@link AuthorizationCheck}. Es gibt genau drei Arten, und ein Host unterscheidet sie mit
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
 * <p>Zum Erzeugen eines Schreibens kennt der Host das Standardschreiben. So kann er je Einstiegspunkt festlegen, welche
 * Standardschreiben zu welchem Bezug zulässig sind. Dass der Bezugstyp zum Standardschreiben passt, hat die Bibliothek
 * vorher schon geprüft.
 */
public sealed interface AuthorizationRequest {

    /** Der angemeldete Benutzer, wie ihn die Spring Security des Hosts kennt. */
    String userId();

    /** Darf der Benutzer diese Handlung an Briefpapier und Standardschreiben ausführen? */
    static AuthorizationRequest forTemplates(String userId, TemplateAction action) {
        return new ForTemplates(userId, action);
    }

    /** Darf der Benutzer diese Handlung an den gespeicherten Schreiben zu diesem fachlichen Bezug ausführen? */
    static AuthorizationRequest forLetter(String userId, LetterAction action, BusinessReference reference) {
        return new ForLetter(userId, action, reference);
    }

    /**
     * Darf der Benutzer aus diesem Standardschreiben zu diesem fachlichen Bezug ein Schreiben erzeugen? Gefragt wird beim
     * Erzeugen der Vorschau und erneut beim Speichern.
     */
    static AuthorizationRequest forCreation(
            String userId, BusinessReference reference, LetterTemplateId letterTemplate) {
        return new ForCreation(userId, reference, letterTemplate);
    }

    /** Siehe {@link #forTemplates(String, TemplateAction)}. */
    record ForTemplates(String userId, TemplateAction action) implements AuthorizationRequest {

        public ForTemplates {
            Require.text(userId, "Die Benutzerkennung");
            Require.value(action, "Die Aktion");
        }
    }

    /** Siehe {@link #forLetter(String, LetterAction, BusinessReference)}. */
    record ForLetter(String userId, LetterAction action, BusinessReference reference)
            implements AuthorizationRequest {

        public ForLetter {
            Require.text(userId, "Die Benutzerkennung");
            Require.value(action, "Die Aktion");
            Require.value(reference, "Der fachliche Bezug");
        }
    }

    /** Siehe {@link #forCreation(String, BusinessReference, LetterTemplateId)}. */
    record ForCreation(String userId, BusinessReference reference, LetterTemplateId letterTemplate)
            implements AuthorizationRequest {

        public ForCreation {
            Require.text(userId, "Die Benutzerkennung");
            Require.value(reference, "Der fachliche Bezug");
            Require.value(letterTemplate, "Das Standardschreiben");
        }
    }
}
