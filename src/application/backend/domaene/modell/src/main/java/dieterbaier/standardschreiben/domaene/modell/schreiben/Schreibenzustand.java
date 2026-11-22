package dieterbaier.standardschreiben.domaene.modell.schreiben;

import dieterbaier.standardschreiben.validierung.Pruefe;
import java.time.Instant;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Ein Schreiben ist sichtbar, bis es logisch gelöscht wird. Ein logisch gelöschtes Schreiben behält sein PDF, erscheint
 * aber in keiner Übersicht und lässt sich weder herunterladen noch erneut löschen; für den Benutzer gibt es es nicht
 * mehr. Ein hart gelöschtes Schreiben hat keinen Zustand, es ist entfernt.
 */
@ValueObject
public sealed interface Schreibenzustand {

    record Sichtbar() implements Schreibenzustand {}

    record LogischGeloescht(Instant am) implements Schreibenzustand {

        public LogischGeloescht {
            Pruefe.pflicht(am, "Der Zeitpunkt des Löschens");
        }
    }
}
