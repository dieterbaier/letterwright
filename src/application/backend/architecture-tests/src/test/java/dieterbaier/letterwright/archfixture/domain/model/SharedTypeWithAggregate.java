package dieterbaier.letterwright.archfixture.domain.model;

import dieterbaier.letterwright.archfixture.domain.model.cyclea.AggregateA;

/** Fixture: absichtlicher Verstoß, ein geteilter Werttyp im Wurzelpaket kennt ein Aggregat. */
public class SharedTypeWithAggregate {

    public AggregateA aggregate() {
        return new AggregateA();
    }
}
