package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.template.TemplateId;

/**
 * Use case "publish template version": the draft of a letterhead or letter template becomes the only published version
 * if its proof PDF with the sample data is conformant (ADR-005, ADR-022). The previously published version is then
 * superseded.
 */
public interface PublishTemplateVersion {

    PublicationResult publish(UserId user, TemplateId template);
}
