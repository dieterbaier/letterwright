package dieterbaier.letterwright.archfixture.adapter.in.web;

import dieterbaier.letterwright.archfixture.usecases.out.OutboundPort;

/** Fixture: deliberate violation, an inbound adapter uses an outbound port. */
public class WebAdapterWithOutboundPort {

    private OutboundPort port;

    public OutboundPort port() {
        return port;
    }
}
