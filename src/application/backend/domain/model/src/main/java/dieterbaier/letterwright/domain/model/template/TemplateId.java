package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.validation.Validate;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Identity of a letterhead or letter template across all versions.
 *
 * <p>The constructor restores an existing identity, {@link #generate()} assigns a new one.
 */
@ValueObject
public record TemplateId(UUID value) {

    public TemplateId {
        Validate.required(value, "The value of the TemplateId");
    }

    public static TemplateId generate() {
        return new TemplateId(UUID.randomUUID());
    }
}
