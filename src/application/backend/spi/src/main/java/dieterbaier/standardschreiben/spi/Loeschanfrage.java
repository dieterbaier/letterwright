package dieterbaier.standardschreiben.spi;

import java.time.Instant;
import java.util.UUID;

/**
 * Anfrage an die {@link Loeschrichtlinie}: Ein Benutzer will dieses Schreiben löschen.
 *
 * @param benutzerkennung der angemeldete Benutzer, wie ihn die Spring Security des Hosts kennt
 * @param schreiben die ID des Schreibens
 * @param erstellt wann das Schreiben gespeichert wurde, etwa für Aufbewahrungsfristen
 */
public record Loeschanfrage(String benutzerkennung, UUID schreiben, FachlicherBezug bezug, Instant erstellt) {

    public Loeschanfrage {
        Pflicht.text(benutzerkennung, "Die Benutzerkennung");
        Pflicht.wert(schreiben, "Die ID des Schreibens");
        Pflicht.wert(bezug, "Der fachliche Bezug");
        Pflicht.wert(erstellt, "Der Erstellungszeitpunkt");
    }
}
