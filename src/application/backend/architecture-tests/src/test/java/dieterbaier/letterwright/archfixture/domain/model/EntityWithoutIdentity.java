package dieterbaier.letterwright.archfixture.domain.model;

import org.jmolecules.ddd.annotation.Entity;

/** Fixture: absichtlicher Verstoß, eine Entität ohne gekennzeichnete Identität. */
@Entity
public class EntityWithoutIdentity {

    private String name = "";

    public String name() {
        return name;
    }
}
