package dieterbaier.letterwright.archfixture.api;

import dieterbaier.letterwright.archfixture.spi.HostRecord;

/** Fixture: deliberate violation, the API uses a type of the SPI. */
public interface ApiWithSpi {

    HostRecord data();
}
