package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.TemplateAction;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;

/** Asks the host whether a user may perform an action. Implemented by {@code adapter-host} (ADR-016). */
public interface Permissions {

    boolean may(UserId user, TemplateAction action);

    boolean may(UserId user, LetterAction action, BusinessReference reference);

    boolean mayCreate(UserId user, BusinessReference reference, LetterTemplate letterTemplate);
}
