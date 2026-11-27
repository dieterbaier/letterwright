package dieterbaier.letterwright.spi;

/**
 * Decides whether a user may perform an action. The host determines roles and visibility.
 *
 * <p>The starter ships a default implementation based on Spring Security that maps each action to a configurable role.
 * It backs off when the host provides its own bean of this interface.
 */
public interface AuthorizationCheck {

    /** {@code true} if the user may perform the action. */
    boolean isAllowed(AuthorizationRequest request);
}
