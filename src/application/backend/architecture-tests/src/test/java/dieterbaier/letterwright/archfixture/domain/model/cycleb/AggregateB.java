package dieterbaier.letterwright.archfixture.domain.model.cycleb;

import dieterbaier.letterwright.archfixture.domain.model.cyclea.AggregateA;

/** Fixture: deliberate violation, counterpart to {@link AggregateA}. */
public class AggregateB {

    public AggregateA other() {
        return new AggregateA();
    }
}
