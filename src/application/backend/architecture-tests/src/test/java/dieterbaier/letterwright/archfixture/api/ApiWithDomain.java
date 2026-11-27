package dieterbaier.letterwright.archfixture.api;

import dieterbaier.letterwright.archfixture.domain.model.ModelWithDomainService;

/** Fixture: absichtlicher Verstoß, die API gibt einen Typ des Domänenmodells heraus. */
public interface ApiWithDomain {

    ModelWithDomainService model();
}
