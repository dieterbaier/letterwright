package dieterbaier.letterwright.archfixture.domain.model.cycleb;

import dieterbaier.letterwright.archfixture.domain.model.cyclea.AggregateA;

/** Fixture: absichtlicher Verstoß, Gegenstück zu {@link AggregateA}. */
public class AggregateB {

    public AggregateA other() {
        return new AggregateA();
    }
}
