package dieterbaier.letterwright.archfixture.domain.model;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/** Fixture: erlaubt, die Identität eines Aggregats am Feld. */
@AggregateRoot
public class AggregateWithIdentityOnField {

    @Identity
    private String id = "";

    public String id() {
        return id;
    }
}
