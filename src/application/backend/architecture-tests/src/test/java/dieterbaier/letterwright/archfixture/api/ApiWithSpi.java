package dieterbaier.letterwright.archfixture.api;

import dieterbaier.letterwright.archfixture.spi.HostRecord;

/** Fixture: absichtlicher Verstoß, die API benutzt einen Typ des SPI. */
public interface ApiWithSpi {

    HostRecord data();
}
