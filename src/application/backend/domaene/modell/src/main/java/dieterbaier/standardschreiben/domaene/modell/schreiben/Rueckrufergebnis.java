package dieterbaier.standardschreiben.domaene.modell.schreiben;

import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Wie die Host-Anwendung auf ein neu angelegtes Schreiben antwortet, wenn sie den optionalen Rückruf nach dem Speichern
 * bereitstellt (ADR-023). Ohne Rückruf gilt jedes Schreiben als angenommen.
 */
@ValueObject
public sealed interface Rueckrufergebnis {

    static Rueckrufergebnis angenommen() {
        return new Angenommen();
    }

    /** Der Host lehnt ab; der Grund wird dem Benutzer angezeigt. */
    static Rueckrufergebnis abgelehnt(String grund) {
        return new Abgelehnt(grund);
    }

    @ValueObject
    record Angenommen() implements Rueckrufergebnis {
    }

    @ValueObject
    record Abgelehnt(String grund) implements Rueckrufergebnis {

        public Abgelehnt {
            Pruefe.nichtBlank(grund, "Der Grund der Ablehnung");
        }
    }
}
