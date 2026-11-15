package io.github.dieterbaier.standardschreiben.archfixture.adapter.out.rendering;

import io.github.dieterbaier.standardschreiben.archfixture.usecases.in.EingehenderPort;

/** Fixture: absichtlicher Verstoß, ein ausgehender Adapter ruft einen Anwendungsfall auf. */
public class RenderingAdapterMitEingehendemPort {

    private EingehenderPort port;

    public EingehenderPort port() {
        return port;
    }
}
