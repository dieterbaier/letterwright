package dieterbaier.letterwright.archfixture.domain;

import org.assertj.core.util.Lists;

/** Fixture: absichtlicher Verstoß, eine Domänenklasse mit einer nicht zugelassenen Bibliothek. */
public class DomainWithForeignLibrary {

    public Object list() {
        return Lists.newArrayList("a");
    }
}
