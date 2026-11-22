package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/** Welche Seite einer Übersicht angefragt ist. Die erste Seite hat die Nummer 0. */
@ValueObject
public record Seitenanfrage(int nummer, int groesse) {

    public Seitenanfrage {
        Pruefe.mindestens(nummer, 0, "Die Seitennummer");
        Pruefe.mindestens(groesse, 1, "Die Seitengröße");
    }

    /** Wie viele Einträge vor dieser Seite liegen. */
    public long versatz() {
        return (long) nummer * groesse;
    }
}
