package dieterbaier.letterwright.archfixture.adapter.in.web;

import dieterbaier.letterwright.archfixture.domain.service.DomainService;

/** Fixture: deliberate violation, an inbound adapter calls a domain service, bypassing the use case. */
public class WebAdapterWithDomainService {

    private DomainService service;
}
