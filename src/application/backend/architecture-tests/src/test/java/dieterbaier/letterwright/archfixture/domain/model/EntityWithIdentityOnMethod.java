package dieterbaier.letterwright.archfixture.domain.model;

import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

/** Fixture: allowed, the identity of an entity on a method. */
@Entity
public class EntityWithIdentityOnMethod {

    @Identity
    public String reference() {
        return "";
    }
}
