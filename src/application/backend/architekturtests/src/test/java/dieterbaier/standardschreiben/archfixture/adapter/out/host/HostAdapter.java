package dieterbaier.standardschreiben.archfixture.adapter.out.host;

import dieterbaier.standardschreiben.archfixture.spi.HostSchnittstelle;
import dieterbaier.standardschreiben.archfixture.usecases.out.AusgehenderPort;

/** Fixture: regelkonform, adapter-host implementiert einen ausgehenden Port und ruft das SPI. */
public class HostAdapter implements AusgehenderPort {

    private HostSchnittstelle host;
}
