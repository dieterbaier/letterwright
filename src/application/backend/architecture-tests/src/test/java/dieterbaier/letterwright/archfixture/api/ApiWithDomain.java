package dieterbaier.letterwright.archfixture.api;

import dieterbaier.letterwright.archfixture.domain.model.ModelWithDomainService;

/** Fixture: deliberate violation, the API exposes a type of the domain model. */
public interface ApiWithDomain {

    ModelWithDomainService model();
}
