package dieterbaier.letterwright.spi;

import java.time.Instant;
import java.util.UUID;

/**
 * Anfrage an die {@link DeletionPolicy}: Ein Benutzer will dieses Schreiben löschen.
 *
 * @param userId der angemeldete Benutzer, wie ihn die Spring Security des Hosts kennt
 * @param letter die ID des Schreibens
 * @param created wann das Schreiben gespeichert wurde, etwa für Aufbewahrungsfristen
 */
public record DeletionRequest(String userId, UUID letter, BusinessReference reference, Instant created) {

    public DeletionRequest {
        Require.text(userId, "Die Benutzerkennung");
        Require.value(letter, "Die ID des Schreibens");
        Require.value(reference, "Der fachliche Bezug");
        Require.value(created, "Der Erstellungszeitpunkt");
    }
}
