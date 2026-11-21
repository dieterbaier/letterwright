package dieterbaier.standardschreiben.domaene.modell.schreiben;

import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/** Wie ein Schreiben gelöscht wird. Lösch- und Aufbewahrungsregeln bestimmt die Host-Anwendung (RB-10, ADR-007). */
@ValueObject
public sealed interface Loeschentscheidung {

    /** Das Schreiben und sein PDF werden entfernt. */
    static Loeschentscheidung hart() {
        return new Hart();
    }

    /** Das Schreiben wird als gelöscht markiert und nicht mehr angezeigt; das PDF bleibt erhalten. */
    static Loeschentscheidung logisch() {
        return new Logisch();
    }

    /** Das Schreiben darf nicht gelöscht werden; der Grund wird dem Benutzer angezeigt. */
    static Loeschentscheidung gesperrt(String grund) {
        return new Gesperrt(grund);
    }

    /** Siehe {@link #hart()}. */
    @ValueObject
    record Hart() implements Loeschentscheidung {
    }

    /** Siehe {@link #logisch()}. */
    @ValueObject
    record Logisch() implements Loeschentscheidung {
    }

    /** Siehe {@link #gesperrt(String)}. */
    @ValueObject
    record Gesperrt(String grund) implements Loeschentscheidung {

        public Gesperrt {
            Pruefe.nichtBlank(grund, "Der Grund der Löschsperre");
        }
    }
}
