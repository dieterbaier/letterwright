package dieterbaier.standardschreiben.spi;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Anfrage an die {@link Berechtigungspruefung}. Zu {@link Aktion#VORLAGEN_PFLEGEN} gibt es keinen fachlichen Bezug, zu
 * allen anderen Aktionen genau einen.
 *
 * @param benutzerkennung der angemeldete Benutzer, wie ihn die Spring Security des Hosts kennt
 */
public record Berechtigungsanfrage(String benutzerkennung, Aktion aktion, @Nullable FachlicherBezug bezug) {

    public Berechtigungsanfrage {
        Pflicht.text(benutzerkennung, "Die Benutzerkennung");
        Pflicht.wert(aktion, "Die Aktion");
        if (aktion.hatBezug() && bezug == null) {
            throw new IllegalArgumentException("Die Aktion " + aktion + " braucht einen fachlichen Bezug");
        }
        if (!aktion.hatBezug() && bezug != null) {
            throw new IllegalArgumentException("Die Aktion " + aktion + " hat keinen fachlichen Bezug");
        }
    }

    /** Der fachliche Bezug, leer bei {@link Aktion#VORLAGEN_PFLEGEN}. */
    public Optional<FachlicherBezug> optionalerBezug() {
        return Optional.ofNullable(bezug);
    }
}
