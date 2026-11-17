package dieterbaier.standardschreiben.archfixture.adapter.out.rendering;

import dieterbaier.standardschreiben.archfixture.spi.HostSchnittstelle;

/** Fixture: absichtlicher Verstoß, ein anderer Adapter als adapter-host ruft das SPI. */
public class RenderingAdapterMitSpi {

    private HostSchnittstelle host;
}
