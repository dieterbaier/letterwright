package dieterbaier.letterwright.archfixture.usecases.in;

import dieterbaier.letterwright.archfixture.usecases.out.OutboundPort;

/** Fixture: absichtlicher Verstoß, ein eingehender Port kennt einen ausgehenden. */
public interface InboundPortWithOutboundPort {

    OutboundPort outbound();
}
