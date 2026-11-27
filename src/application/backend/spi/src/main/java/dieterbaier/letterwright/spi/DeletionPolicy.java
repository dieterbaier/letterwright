package dieterbaier.letterwright.spi;

/**
 * Decides whether and how a letter is deleted. The host determines the deletion and retention rules (RB-10). The host
 * implements this interface as a bean.
 *
 * <p>The library asks only after the {@link AuthorizationCheck} has allowed {@link LetterAction#DELETE}.
 */
public interface DeletionPolicy {

    /** How the letter is deleted. */
    DeletionDecision decide(DeletionRequest request);
}
