package dieterbaier.letterwright.archfixture.adapter.out.rendering;

import dieterbaier.letterwright.archfixture.starter.FixtureAutoConfiguration;

/** Fixture: deliberate violation, an adapter depends on the starter. */
public class RenderingAdapterWithStarter {

    private final FixtureAutoConfiguration configuration = new FixtureAutoConfiguration();

    public FixtureAutoConfiguration configuration() {
        return configuration;
    }
}
