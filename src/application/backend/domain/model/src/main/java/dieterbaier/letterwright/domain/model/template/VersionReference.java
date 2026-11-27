package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Verweist von außen auf genau eine Version eines Briefpapiers oder Standardschreibens. Snapshot und Schreiben halten
 * nur diesen Verweis, nie die Version selbst; sie gehört zum Aggregat ihrer Vorlage.
 */
@ValueObject
public record VersionReference(TemplateId template, VersionNumber number) {

    public VersionReference {
        Validate.required(template, "Die Vorlage des Versionsverweises");
        Validate.required(number, "Die Nummer des Versionsverweises");
    }
}
