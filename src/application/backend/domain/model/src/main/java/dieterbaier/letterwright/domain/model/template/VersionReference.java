package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Refers from outside to exactly one version of a letterhead or letter template. Snapshot and letter hold only this
 * reference, never the version itself; it belongs to the aggregate of its template.
 */
@ValueObject
public record VersionReference(TemplateId template, VersionNumber number) {

    public VersionReference {
        Validate.required(template, "The template of the VersionReference");
        Validate.required(number, "The number of the VersionReference");
    }
}
