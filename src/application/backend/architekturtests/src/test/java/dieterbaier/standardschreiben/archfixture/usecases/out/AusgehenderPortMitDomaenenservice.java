package dieterbaier.standardschreiben.archfixture.usecases.out;

import dieterbaier.standardschreiben.archfixture.domaene.service.Domaenenservice;

/** Fixture: absichtlicher Verstoß, ein ausgehender Port kennt einen Domänenservice. */
public interface AusgehenderPortMitDomaenenservice {

    Domaenenservice service();
}
