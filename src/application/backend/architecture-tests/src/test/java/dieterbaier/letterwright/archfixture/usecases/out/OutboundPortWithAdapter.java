package dieterbaier.letterwright.archfixture.usecases.out;

import dieterbaier.letterwright.archfixture.adapter.in.web.WebAdapter;

/** Fixture: deliberate violation, a port knows an adapter. */
public interface OutboundPortWithAdapter {

    WebAdapter adapter();
}
