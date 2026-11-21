package dieterbaier.standardschreiben.archfixture.domaene.modell.zyklusb;

import dieterbaier.standardschreiben.archfixture.domaene.modell.zyklusa.AggregatA;

/** Fixture: absichtlicher Verstoß, Gegenstück zu {@link AggregatA}. */
public class AggregatB {

    public AggregatA anderes() {
        return new AggregatA();
    }
}
