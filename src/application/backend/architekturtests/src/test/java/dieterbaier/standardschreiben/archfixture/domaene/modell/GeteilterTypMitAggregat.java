package dieterbaier.standardschreiben.archfixture.domaene.modell;

import dieterbaier.standardschreiben.archfixture.domaene.modell.zyklusa.AggregatA;

/** Fixture: absichtlicher Verstoß, ein geteilter Werttyp im Wurzelpaket kennt ein Aggregat. */
public class GeteilterTypMitAggregat {

    public AggregatA aggregat() {
        return new AggregatA();
    }
}
