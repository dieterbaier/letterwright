package dieterbaier.letterwright.domain.model;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/** Welche Seite einer Übersicht angefragt ist. Die erste Seite hat die Nummer 0. */
@ValueObject
public record PageRequest(int number, int size) {

    public PageRequest {
        Validate.atLeast(number, 0, "Die Seitennummer");
        Validate.atLeast(size, 1, "Die Seitengröße");
    }

    /** Wie viele Einträge vor dieser Seite liegen. */
    public long offset() {
        return (long) number * size;
    }
}
