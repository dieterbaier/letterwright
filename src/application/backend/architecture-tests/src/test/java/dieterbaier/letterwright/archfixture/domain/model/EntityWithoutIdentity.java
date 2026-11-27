package dieterbaier.letterwright.archfixture.domain.model;

import org.jmolecules.ddd.annotation.Entity;

/** Fixture: deliberate violation, an entity without a marked identity. */
@Entity
public class EntityWithoutIdentity {

    private String name = "";

    public String name() {
        return name;
    }
}
