package dieterbaier.letterwright.archfixture.usecases.out;

import dieterbaier.letterwright.archfixture.spi.HostInterface;

/** Fixture: absichtlicher Verstoß, ein ausgehender Port kennt das SPI. */
public interface OutboundPortWithSpi {

    HostInterface host();
}
