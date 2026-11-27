package dieterbaier.letterwright.archfixture.adapter.out.host;

import dieterbaier.letterwright.archfixture.spi.HostInterface;
import dieterbaier.letterwright.archfixture.usecases.out.OutboundPort;

/** Fixture: compliant, adapter-host implements an outbound port and calls the SPI. */
public class HostAdapter implements OutboundPort {

    private HostInterface host;
}
