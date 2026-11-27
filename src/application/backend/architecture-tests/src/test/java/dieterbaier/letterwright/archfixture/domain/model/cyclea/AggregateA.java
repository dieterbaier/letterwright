package dieterbaier.letterwright.archfixture.domain.model.cyclea;

import dieterbaier.letterwright.archfixture.domain.model.cycleb.AggregateB;

/** Fixture: absichtlicher Verstoß, zwei Aggregatpakete kennen sich gegenseitig. */
public class AggregateA {

    public AggregateB other() {
        return new AggregateB();
    }
}
