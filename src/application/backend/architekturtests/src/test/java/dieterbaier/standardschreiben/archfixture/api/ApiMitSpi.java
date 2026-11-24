package dieterbaier.standardschreiben.archfixture.api;

import dieterbaier.standardschreiben.archfixture.spi.HostDatensatz;

/** Fixture: absichtlicher Verstoß, die API benutzt einen Typ des SPI. */
public interface ApiMitSpi {

    HostDatensatz daten();
}
