package dieterbaier.letterwright.archfixture.domain.model;

import dieterbaier.letterwright.archfixture.domain.service.DomainService;

/** Fixture: deliberate violation, a model object knows a domain service. */
public class ModelWithDomainService {

    private DomainService service;
}
