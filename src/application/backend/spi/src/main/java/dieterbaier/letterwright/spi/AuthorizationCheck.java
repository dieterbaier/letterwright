package dieterbaier.letterwright.spi;

/**
 * Entscheidet, ob ein Benutzer eine Aktion ausführen darf. Rollen und Sichtbarkeit bestimmt der Host.
 *
 * <p>Der Starter bringt eine Default-Implementierung auf Spring Security mit, die jede Aktion auf eine konfigurierbare
 * Rolle abbildet. Stellt der Host eine eigene Bean dieser Schnittstelle bereit, tritt sie zurück.
 */
public interface AuthorizationCheck {

    /** {@code true}, wenn der Benutzer die Aktion ausführen darf. */
    boolean isAllowed(AuthorizationRequest request);
}
