package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.validation.Validate;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Identity of a stored letter.
 *
 * <p>The constructor restores an existing identity, {@link #generate()} assigns a new one.
 */
@ValueObject
public record LetterId(UUID value) {

    public LetterId {
        Validate.required(value, "The value of the LetterId");
    }

    public static LetterId generate() {
        return new LetterId(UUID.randomUUID());
    }
}
