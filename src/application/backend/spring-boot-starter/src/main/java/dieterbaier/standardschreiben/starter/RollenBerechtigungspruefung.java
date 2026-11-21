package dieterbaier.standardschreiben.starter;

import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Berechtigungspruefung;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Default-{@link Berechtigungspruefung} auf Spring Security: Erlaubt ist eine Aktion, wenn der angemeldete Benutzer der
 * angefragte ist und die Authority der Aktion hat (siehe {@link Berechtigungseigenschaften}).
 *
 * <p>Den fachlichen Bezug wertet sie nicht aus. Wer Schreiben nach Bezug sichtbar machen will, etwa nur die eigenen
 * Kunden, stellt eine eigene Bean bereit; dann tritt diese zurück.
 */
final class RollenBerechtigungspruefung implements Berechtigungspruefung {

    private final Berechtigungseigenschaften eigenschaften;

    RollenBerechtigungspruefung(Berechtigungseigenschaften eigenschaften) {
        this.eigenschaften = eigenschaften;
    }

    @Override
    public boolean istErlaubt(Berechtigungsanfrage anfrage) {
        Authentication anmeldung = SecurityContextHolder.getContext().getAuthentication();
        if (anmeldung == null || !anmeldung.isAuthenticated()
                || !anfrage.benutzerkennung().equals(anmeldung.getName())) {
            return false;
        }
        String rolle = eigenschaften.rolle(anfrage.aktion());
        return anmeldung.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(rolle::equals);
    }
}
