package dieterbaier.standardschreiben.archfixture.usecases.out;

import dieterbaier.standardschreiben.archfixture.spi.HostSchnittstelle;

/** Fixture: absichtlicher Verstoß, ein ausgehender Port kennt das SPI. */
public interface AusgehenderPortMitSpi {

    HostSchnittstelle host();
}
