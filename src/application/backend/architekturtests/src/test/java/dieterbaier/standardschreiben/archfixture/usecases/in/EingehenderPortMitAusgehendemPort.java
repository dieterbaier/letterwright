package dieterbaier.standardschreiben.archfixture.usecases.in;

import dieterbaier.standardschreiben.archfixture.usecases.out.AusgehenderPort;

/** Fixture: absichtlicher Verstoß, ein eingehender Port kennt einen ausgehenden. */
public interface EingehenderPortMitAusgehendemPort {

    AusgehenderPort ausgehend();
}
