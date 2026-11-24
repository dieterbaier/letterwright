package dieterbaier.standardschreiben.archfixture.api;

import dieterbaier.standardschreiben.archfixture.domaene.modell.ModellMitDomaenenservice;

/** Fixture: absichtlicher Verstoß, die API gibt einen Typ des Domänenmodells heraus. */
public interface ApiMitDomaene {

    ModellMitDomaenenservice modell();
}
