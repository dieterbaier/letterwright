package dieterbaier.letterwright.archfixture.usecases.in;

import dieterbaier.letterwright.archfixture.usecases.out.OutboundPort;

/** Fixture: deliberate violation, an inbound port knows an outbound one. */
public interface InboundPortWithOutboundPort {

    OutboundPort outbound();
}
