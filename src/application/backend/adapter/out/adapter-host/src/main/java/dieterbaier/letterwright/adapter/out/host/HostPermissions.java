package dieterbaier.letterwright.adapter.out.host;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.TemplateAction;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.spi.AuthorizationRequest;
import dieterbaier.letterwright.spi.AuthorizationCheck;
import dieterbaier.letterwright.usecases.out.Permissions;

/** Asks the host's {@link AuthorizationCheck}, or the starter's default implementation. */
public final class HostPermissions implements Permissions {

    private final AuthorizationCheck check;

    public HostPermissions(AuthorizationCheck check) {
        this.check = check;
    }

    @Override
    public boolean may(UserId user, TemplateAction action) {
        return check.isAllowed(AuthorizationRequest.forTemplates(user.value(), Mapping.toSpi(action)));
    }

    @Override
    public boolean may(UserId user, LetterAction action, BusinessReference reference) {
        return check.isAllowed(AuthorizationRequest.forLetter(user.value(), Mapping.toSpi(action),
                Mapping.toSpi(reference)));
    }

    @Override
    public boolean mayCreate(UserId user, BusinessReference reference, LetterTemplate letterTemplate) {
        return check.isAllowed(AuthorizationRequest.forCreation(user.value(), Mapping.toSpi(reference),
                Mapping.id(letterTemplate)));
    }
}
