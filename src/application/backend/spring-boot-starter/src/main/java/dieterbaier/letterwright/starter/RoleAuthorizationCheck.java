package dieterbaier.letterwright.starter;

import dieterbaier.letterwright.spi.AuthorizationRequest;
import dieterbaier.letterwright.spi.AuthorizationCheck;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Default {@link AuthorizationCheck} on Spring Security: an action is allowed if the logged-in user is the requested
 * one and has the authority of the action (see {@link AuthorizationProperties}).
 *
 * <p>An anonymous access does not count as a login, even if Spring Security treats it as authenticated and its
 * authority is mapped to an action. What is anonymous is decided by the host's {@link AuthenticationTrustResolver},
 * if it provides one as a bean.
 *
 * <p>It does not evaluate the business reference. Whoever wants to make letters visible by reference, for example
 * only one's own customers, provides a bean of their own; then this one steps back.
 */
final class RoleAuthorizationCheck implements AuthorizationCheck {

    private final AuthorizationProperties properties;
    private final AuthenticationTrustResolver trust;

    RoleAuthorizationCheck(AuthorizationProperties properties, AuthenticationTrustResolver trust) {
        this.properties = properties;
        this.trust = trust;
    }

    @Override
    public boolean isAllowed(AuthorizationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || trust.isAnonymous(authentication)
                || !request.userId().equals(authentication.getName())) {
            return false;
        }
        String role = properties.role(request);
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }
}
