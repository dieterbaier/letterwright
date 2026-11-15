package dieterbaier.standardschreiben.archfixture.adapter.in.web;

import dieterbaier.standardschreiben.archfixture.usecases.out.AusgehenderPort;

/** Fixture: absichtlicher Verstoß, ein eingehender Adapter nutzt einen ausgehenden Port. */
public class WebAdapterMitAusgehendemPort {

    private AusgehenderPort port;

    public AusgehenderPort port() {
        return port;
    }
}
