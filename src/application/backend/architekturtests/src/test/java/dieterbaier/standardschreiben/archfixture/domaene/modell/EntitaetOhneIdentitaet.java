package dieterbaier.standardschreiben.archfixture.domaene.modell;

import org.jmolecules.ddd.annotation.Entity;

/** Fixture: absichtlicher Verstoß, eine Entität ohne gekennzeichnete Identität. */
@Entity
public class EntitaetOhneIdentitaet {

    private String name = "";

    public String name() {
        return name;
    }
}
