package dieterbaier.standardschreiben.archfixture.domaene.modell.zyklusa;

import dieterbaier.standardschreiben.archfixture.domaene.modell.zyklusb.AggregatB;

/** Fixture: absichtlicher Verstoß, zwei Aggregatpakete kennen sich gegenseitig. */
public class AggregatA {

    public AggregatB anderes() {
        return new AggregatB();
    }
}
