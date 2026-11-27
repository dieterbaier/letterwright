package dieterbaier.letterwright.domain.model;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Kennung des angemeldeten Benutzers, wie sie die Host-Anwendung liefert. Die Bibliothek verwaltet keine Benutzer, sie
 * vergleicht nur Kennungen.
 */
@ValueObject
public record UserId(String value) {

    public UserId {
        Validate.notBlank(value, "Die Benutzerkennung");
    }
}
