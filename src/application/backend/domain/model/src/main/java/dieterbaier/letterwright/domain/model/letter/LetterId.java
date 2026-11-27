package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.validation.Validate;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Identität eines gespeicherten Schreibens.
 *
 * <p>Der Konstruktor stellt eine bestehende Identität wieder her, {@link #generate()} vergibt eine neue.
 */
@ValueObject
public record LetterId(UUID value) {

    public LetterId {
        Validate.required(value, "Der Wert der SchreibenId");
    }

    public static LetterId generate() {
        return new LetterId(UUID.randomUUID());
    }
}
