package dieterbaier.letterwright.archfixture.usecases.out;

import dieterbaier.letterwright.archfixture.spi.HostInterface;

/** Fixture: deliberate violation, an outbound port knows the SPI. */
public interface OutboundPortWithSpi {

    HostInterface host();
}
