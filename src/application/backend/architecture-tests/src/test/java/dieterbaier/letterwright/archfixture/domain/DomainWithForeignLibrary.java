package dieterbaier.letterwright.archfixture.domain;

import org.assertj.core.util.Lists;

/** Fixture: deliberate violation, a domain class with an unapproved library. */
public class DomainWithForeignLibrary {

    public Object list() {
        return Lists.newArrayList("a");
    }
}
