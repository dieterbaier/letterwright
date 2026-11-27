package dieterbaier.letterwright.archfixture.domain.model.cyclea;

import dieterbaier.letterwright.archfixture.domain.model.cycleb.AggregateB;

/** Fixture: deliberate violation, two aggregate packages know each other. */
public class AggregateA {

    public AggregateB other() {
        return new AggregateB();
    }
}
