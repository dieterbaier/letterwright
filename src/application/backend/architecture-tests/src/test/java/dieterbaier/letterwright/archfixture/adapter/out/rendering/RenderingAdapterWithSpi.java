package dieterbaier.letterwright.archfixture.adapter.out.rendering;

import dieterbaier.letterwright.archfixture.spi.HostInterface;

/** Fixture: deliberate violation, an adapter other than adapter-host calls the SPI. */
public class RenderingAdapterWithSpi {

    private HostInterface host;
}
