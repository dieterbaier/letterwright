package dieterbaier.letterwright.archfixture.adapter.out.rendering;

import dieterbaier.letterwright.archfixture.usecases.in.InboundPort;

/** Fixture: deliberate violation, an outbound adapter calls a use case. */
public class RenderingAdapterWithInboundPort {

    private InboundPort port;

    public InboundPort port() {
        return port;
    }
}
