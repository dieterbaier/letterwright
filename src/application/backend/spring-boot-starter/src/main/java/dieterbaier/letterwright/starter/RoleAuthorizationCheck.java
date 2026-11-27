package dieterbaier.letterwright.starter;

import dieterbaier.letterwright.spi.AuthorizationRequest;
import dieterbaier.letterwright.spi.AuthorizationCheck;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Default-{@link AuthorizationCheck} auf Spring Security: Erlaubt ist eine Aktion, wenn der angemeldete Benutzer der
 * angefragte ist und die Authority der Aktion hat (siehe {@link AuthorizationProperties}).
 *
 * <p>Ein anonymer Zugriff gilt nicht als Anmeldung, auch wenn Spring Security ihn als authentifiziert führt und seine
 * Authority auf eine Aktion abgebildet ist. Was anonym ist, entscheidet der {@link AuthenticationTrustResolver} des
 * Hosts, sofern er einen als Bean bereitstellt.
 *
 * <p>Den fachlichen Bezug wertet sie nicht aus. Wer Schreiben nach Bezug sichtbar machen will, etwa nur die eigenen
 * Kunden, stellt eine eigene Bean bereit; dann tritt diese zurück.
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
