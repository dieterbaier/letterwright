package dieterbaier.letterwright.archfixture.domain.model;

import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

/** Fixture: erlaubt, die Identität einer Entität an einer Methode. */
@Entity
public class EntityWithIdentityOnMethod {

    @Identity
    public String reference() {
        return "";
    }
}
