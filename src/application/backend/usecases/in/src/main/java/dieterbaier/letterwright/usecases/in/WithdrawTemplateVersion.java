package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.template.TemplateId;

/**
 * Use case "withdraw template version": the published version of a letterhead or letter template is withdrawn without a
 * successor. Until the next publication, no new letter is created with the template; saved letters stay unchanged
 * (ADR-007, ADR-022).
 */
public interface WithdrawTemplateVersion {

    WithdrawalResult withdraw(UserId user, TemplateId template);
}
