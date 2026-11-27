package dieterbaier.letterwright.archfixture.domain.model;

import dieterbaier.letterwright.archfixture.domain.model.cyclea.AggregateA;

/** Fixture: deliberate violation, a shared value type in the root package knows an aggregate. */
public class SharedTypeWithAggregate {

    public AggregateA aggregate() {
        return new AggregateA();
    }
}
