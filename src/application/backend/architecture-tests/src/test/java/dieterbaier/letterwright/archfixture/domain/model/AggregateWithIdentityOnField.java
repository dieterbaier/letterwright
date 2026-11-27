package dieterbaier.letterwright.archfixture.domain.model;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/** Fixture: allowed, the identity of an aggregate on a field. */
@AggregateRoot
public class AggregateWithIdentityOnField {

    @Identity
    private String id = "";

    public String id() {
        return id;
    }
}
