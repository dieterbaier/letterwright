package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Kennung des angemeldeten Benutzers, wie sie die Host-Anwendung liefert. Die Bibliothek verwaltet keine Benutzer, sie
 * vergleicht nur Kennungen.
 */
@ValueObject
public record BenutzerKennung(String wert) {

    public BenutzerKennung {
        Pruefe.nichtBlank(wert, "Die Benutzerkennung");
    }
}
