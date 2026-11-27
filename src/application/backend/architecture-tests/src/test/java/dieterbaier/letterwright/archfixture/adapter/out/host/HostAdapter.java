package dieterbaier.letterwright.archfixture.adapter.out.host;

import dieterbaier.letterwright.archfixture.spi.HostInterface;
import dieterbaier.letterwright.archfixture.usecases.out.OutboundPort;

/** Fixture: regelkonform, adapter-host implementiert einen ausgehenden Port und ruft das SPI. */
public class HostAdapter implements OutboundPort {

    private HostInterface host;
}
