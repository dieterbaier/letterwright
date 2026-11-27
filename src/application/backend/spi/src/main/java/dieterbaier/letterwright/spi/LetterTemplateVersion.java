package dieterbaier.letterwright.spi;

import java.util.UUID;

/**
 * Die Version des Standardschreibens, aus der ein Schreiben entstand. Versionen sind unveränderlich; die ID bleibt über
 * alle Versionen eines Standardschreibens gleich und ist dieselbe wie in {@link LetterTemplateId}.
 *
 * @param id die ID des Standardschreibens
 * @param number die laufende Nummer der Version, beginnend bei 1
 */
public record LetterTemplateVersion(UUID id, int number) {

    public LetterTemplateVersion {
        Require.value(id, "Die ID des Standardschreibens");
        if (number < 1) {
            throw new IllegalArgumentException("Die Versionsnummer muss mindestens 1 sein, ist aber " + number);
        }
    }
}
