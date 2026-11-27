package dieterbaier.letterwright.archfixture.adapter.out.rendering;

import dieterbaier.letterwright.archfixture.starter.FixtureAutoConfiguration;

/** Fixture: absichtlicher Verstoß, ein Adapter hängt vom Starter ab. */
public class RenderingAdapterWithStarter {

    private final FixtureAutoConfiguration configuration = new FixtureAutoConfiguration();

    public FixtureAutoConfiguration configuration() {
        return configuration;
    }
}
