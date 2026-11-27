package dieterbaier.letterwright.archfixture.adapter.out.rendering;

import dieterbaier.letterwright.archfixture.usecases.in.InboundPort;

/** Fixture: absichtlicher Verstoß, ein ausgehender Adapter ruft einen Anwendungsfall auf. */
public class RenderingAdapterWithInboundPort {

    private InboundPort port;

    public InboundPort port() {
        return port;
    }
}
