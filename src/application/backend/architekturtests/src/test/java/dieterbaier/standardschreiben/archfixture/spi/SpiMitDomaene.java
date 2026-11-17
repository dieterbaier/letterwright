package dieterbaier.standardschreiben.archfixture.spi;

import dieterbaier.standardschreiben.archfixture.domaene.modell.ModellMitDomaenenservice;

/** Fixture: absichtlicher Verstoß, das SPI gibt einen Typ des Domänenmodells heraus. */
public interface SpiMitDomaene {

    ModellMitDomaenenservice modell();
}
