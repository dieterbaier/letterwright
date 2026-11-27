package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.validation.Validate;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Identität eines Briefpapiers oder Standardschreibens über alle Versionen hinweg.
 *
 * <p>Der Konstruktor stellt eine bestehende Identität wieder her, {@link #generate()} vergibt eine neue.
 */
@ValueObject
public record TemplateId(UUID value) {

    public TemplateId {
        Validate.required(value, "Der Wert der VorlagenId");
    }

    public static TemplateId generate() {
        return new TemplateId(UUID.randomUUID());
    }
}
