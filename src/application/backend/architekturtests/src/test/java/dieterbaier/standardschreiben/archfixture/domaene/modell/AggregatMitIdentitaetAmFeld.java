package dieterbaier.standardschreiben.archfixture.domaene.modell;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/** Fixture: erlaubt, die Identität eines Aggregats am Feld. */
@AggregateRoot
public class AggregatMitIdentitaetAmFeld {

    @Identity
    private String id = "";

    public String id() {
        return id;
    }
}
