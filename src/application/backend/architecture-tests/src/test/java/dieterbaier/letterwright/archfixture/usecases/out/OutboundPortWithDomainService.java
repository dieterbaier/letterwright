package dieterbaier.letterwright.archfixture.usecases.out;

import dieterbaier.letterwright.archfixture.domain.service.DomainService;

/** Fixture: absichtlicher Verstoß, ein ausgehender Port kennt einen Domänenservice. */
public interface OutboundPortWithDomainService {

    DomainService service();
}
