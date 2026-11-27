package dieterbaier.letterwright.archfixture.validation;

import dieterbaier.letterwright.archfixture.domain.DomainWithAdapter;

/** Fixture: absichtlicher Verstoß, das allgemeine Prüfmodul kennt die Domäne. */
public class ValidationWithDomain {

    public DomainWithAdapter domain() {
        return new DomainWithAdapter();
    }
}
