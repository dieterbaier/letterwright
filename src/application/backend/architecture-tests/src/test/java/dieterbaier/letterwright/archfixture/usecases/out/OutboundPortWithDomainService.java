package dieterbaier.letterwright.archfixture.usecases.out;

import dieterbaier.letterwright.archfixture.domain.service.DomainService;

/** Fixture: deliberate violation, an outbound port knows a domain service. */
public interface OutboundPortWithDomainService {

    DomainService service();
}
