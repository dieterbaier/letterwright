package dieterbaier.letterwright.archfixture.validation;

import dieterbaier.letterwright.archfixture.domain.DomainWithAdapter;

/** Fixture: deliberate violation, the general validation module knows the domain. */
public class ValidationWithDomain {

    public DomainWithAdapter domain() {
        return new DomainWithAdapter();
    }
}
