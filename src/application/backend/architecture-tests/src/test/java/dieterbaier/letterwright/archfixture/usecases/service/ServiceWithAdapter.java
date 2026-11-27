package dieterbaier.letterwright.archfixture.usecases.service;

import dieterbaier.letterwright.archfixture.adapter.out.rendering.RenderingAdapter;

/** Fixture: deliberate violation, the implementation of the use cases accesses an adapter directly. */
public class ServiceWithAdapter {

    private final RenderingAdapter adapter = new RenderingAdapter();

    public RenderingAdapter adapter() {
        return adapter;
    }
}
