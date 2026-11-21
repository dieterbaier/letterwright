package dieterbaier.standardschreiben.archfixture.domaene.modell;

import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

/** Fixture: erlaubt, die Identität einer Entität an einer Methode. */
@Entity
public class EntitaetMitIdentitaetAnMethode {

    @Identity
    public String verweis() {
        return "";
    }
}
