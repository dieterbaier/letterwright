package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/** Verweis auf das in der Dokumentablage abgelegte PDF eines Schreibens. Was er bedeutet, weiß nur die Ablage. */
@ValueObject
public record DocumentReference(String value) {

    public DocumentReference {
        Validate.notBlank(value, "Der Dokumentverweis");
    }
}
