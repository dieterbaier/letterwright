package dieterbaier.standardschreiben.domaene.modell.vorlage;

import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/** Laufende Nummer einer Vorlagenversion, beginnend bei 1. */
@ValueObject
public record Versionsnummer(int wert) implements Comparable<Versionsnummer> {

    /** Die Nummer der ersten Version einer Vorlage. */
    public static final Versionsnummer ERSTE = new Versionsnummer(1);

    public Versionsnummer {
        Pruefe.mindestens(wert, 1, "Die Versionsnummer");
    }

    /** Die Nummer der nächsten Version. */
    public Versionsnummer naechste() {
        return new Versionsnummer(Math.addExact(wert, 1));
    }

    @Override
    public int compareTo(Versionsnummer andere) {
        return Integer.compare(wert, andere.wert);
    }
}
