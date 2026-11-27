package dieterbaier.letterwright.archfixture.adapter.in.web;

import dieterbaier.letterwright.archfixture.usecases.out.OutboundPort;

/** Fixture: absichtlicher Verstoß, ein eingehender Adapter nutzt einen ausgehenden Port. */
public class WebAdapterWithOutboundPort {

    private OutboundPort port;

    public OutboundPort port() {
        return port;
    }
}
