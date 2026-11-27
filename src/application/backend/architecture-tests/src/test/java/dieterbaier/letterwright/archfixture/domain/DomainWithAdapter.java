package dieterbaier.letterwright.archfixture.domain;

import dieterbaier.letterwright.archfixture.adapter.out.rendering.RenderingAdapter;

/** Fixture: deliberate violation, a domain class uses an adapter. */
public class DomainWithAdapter {

    private RenderingAdapter rendering;
}
