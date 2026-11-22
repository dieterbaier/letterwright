package dieterbaier.standardschreiben.starter;

import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Berechtigungspruefung;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Default-{@link Berechtigungspruefung} auf Spring Security: Erlaubt ist eine Aktion, wenn der angemeldete Benutzer der
 * angefragte ist und die Authority der Aktion hat (siehe {@link Berechtigungseigenschaften}).
 *
 * <p>Ein anonymer Zugriff gilt nicht als Anmeldung, auch wenn Spring Security ihn als authentifiziert führt und seine
 * Authority auf eine Aktion abgebildet ist. Was anonym ist, entscheidet der {@link AuthenticationTrustResolver} des
 * Hosts, sofern er einen als Bean bereitstellt.
 *
 * <p>Den fachlichen Bezug wertet sie nicht aus. Wer Schreiben nach Bezug sichtbar machen will, etwa nur die eigenen
 * Kunden, stellt eine eigene Bean bereit; dann tritt diese zurück.
 */
final class RollenBerechtigungspruefung implements Berechtigungspruefung {

    private final Berechtigungseigenschaften eigenschaften;
    private final AuthenticationTrustResolver vertrauen;

    RollenBerechtigungspruefung(Berechtigungseigenschaften eigenschaften, AuthenticationTrustResolver vertrauen) {
        this.eigenschaften = eigenschaften;
        this.vertrauen = vertrauen;
    }

    @Override
    public boolean istErlaubt(Berechtigungsanfrage anfrage) {
        Authentication anmeldung = SecurityContextHolder.getContext().getAuthentication();
        if (anmeldung == null || !anmeldung.isAuthenticated() || vertrauen.isAnonymous(anmeldung)
                || !anfrage.benutzerkennung().equals(anmeldung.getName())) {
            return false;
        }
        String rolle = eigenschaften.rolle(anfrage);
        return anmeldung.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(rolle::equals);
    }
}
