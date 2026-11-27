package dieterbaier.letterwright.archfixture.usecases.out;

import dieterbaier.letterwright.archfixture.adapter.in.web.WebAdapter;

/** Fixture: absichtlicher Verstoß, ein Port kennt einen Adapter. */
public interface OutboundPortWithAdapter {

    WebAdapter adapter();
}
