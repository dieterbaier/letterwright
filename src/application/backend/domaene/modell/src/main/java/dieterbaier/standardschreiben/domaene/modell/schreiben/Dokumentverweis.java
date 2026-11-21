package dieterbaier.standardschreiben.domaene.modell.schreiben;

import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/** Verweis auf das in der Dokumentablage abgelegte PDF eines Schreibens. Was er bedeutet, weiß nur die Ablage. */
@ValueObject
public record Dokumentverweis(String wert) {

    public Dokumentverweis {
        Pruefe.nichtBlank(wert, "Der Dokumentverweis");
    }
}
