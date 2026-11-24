package dieterbaier.standardschreiben.api;

import java.util.UUID;

/**
 * Die Version des Standardschreibens, aus der das Schreiben entstand. Versionen sind unveränderlich; die ID bleibt über
 * alle Versionen eines Standardschreibens gleich.
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
