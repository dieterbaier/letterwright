package dieterbaier.standardschreiben.spi;

import java.util.UUID;

/**
 * Die Version des Standardschreibens, aus der ein Schreiben entstand. Versionen sind unveränderlich; die ID bleibt über
 * alle Versionen eines Standardschreibens gleich und ist dieselbe wie in {@link Standardschreibenkennung}.
 *
 * @param id die ID des Standardschreibens
 * @param nummer die laufende Nummer der Version, beginnend bei 1
 */
public record Standardschreibenversion(UUID id, int nummer) {

    public Standardschreibenversion {
        Pflicht.wert(id, "Die ID des Standardschreibens");
        if (nummer < 1) {
            throw new IllegalArgumentException("Die Versionsnummer muss mindestens 1 sein, ist aber " + nummer);
        }
    }
}
