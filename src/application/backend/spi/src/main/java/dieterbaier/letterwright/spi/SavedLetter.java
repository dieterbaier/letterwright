package dieterbaier.letterwright.spi;

import java.time.Instant;
import java.util.UUID;

/**
 * Die stabilen Identifikatoren eines gespeicherten Schreibens, die der {@link LetterSaveCallback Rückruf} erhält
 * (ADR-023). Über die ID verknüpft der Host seine eigenen Daten mit dem Schreiben.
 *
 * @param id die ID des Schreibens, ein technischer Schlüssel, der sich nie ändert
 * @param reference der fachliche Bezug, zu dem das Schreiben erzeugt wurde
 * @param letterTemplate die Version des Standardschreibens, aus der es entstand
 * @param userId der Benutzer, der die Vorschau erzeugt und gespeichert hat, wie ihn die Spring Security des
 *     Hosts kennt
 * @param created der Zeitpunkt der Erstellung
 */
public record SavedLetter(
        UUID id, BusinessReference reference, LetterTemplateVersion letterTemplate, String userId,
        Instant created) {

    public SavedLetter {
        Require.value(id, "Die ID des Schreibens");
        Require.value(reference, "Der fachliche Bezug");
        Require.value(letterTemplate, "Das Standardschreiben");
        Require.text(userId, "Die Benutzerkennung");
        Require.value(created, "Der Zeitpunkt der Erstellung");
    }
}
