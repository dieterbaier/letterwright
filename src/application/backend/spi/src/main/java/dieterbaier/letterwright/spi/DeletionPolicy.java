package dieterbaier.letterwright.spi;

/**
 * Entscheidet, ob und wie ein Schreiben gelöscht wird. Lösch- und Aufbewahrungsregeln bestimmt der Host (RB-10). Der
 * Host implementiert diese Schnittstelle als Bean.
 *
 * <p>Die Bibliothek fragt erst, nachdem die {@link AuthorizationCheck} {@link LetterAction#DELETE} erlaubt hat.
 */
public interface DeletionPolicy {

    /** Wie das Schreiben gelöscht wird. */
    DeletionDecision decide(DeletionRequest request);
}
