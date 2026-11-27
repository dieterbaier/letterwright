package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.template.TemplateId;

/**
 * Anwendungsfall „Vorlagenversion zurückziehen“: Die veröffentlichte Version eines Briefpapiers oder Standardschreibens
 * wird ohne Nachfolger zurückgenommen. Bis zur nächsten Veröffentlichung entsteht mit der Vorlage kein neues Schreiben;
 * gespeicherte Schreiben bleiben unverändert (ADR-007, ADR-022).
 */
public interface WithdrawTemplateVersion {

    WithdrawalResult withdraw(UserId user, TemplateId template);
}
