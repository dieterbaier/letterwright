package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.template.TemplateId;

/**
 * Anwendungsfall „Vorlagenversion veröffentlichen“: Der Entwurf eines Briefpapiers oder Standardschreibens wird zur
 * einzigen veröffentlichten Version, wenn sein Probe-PDF mit den Beispieldaten konform ist (ADR-005, ADR-022). Die bisher
 * veröffentlichte Version ist danach abgelöst.
 */
public interface PublishTemplateVersion {

    PublicationResult publish(UserId user, TemplateId template);
}
