package dieterbaier.letterwright.archfixture.usecases.service;

import dieterbaier.letterwright.archfixture.adapter.out.rendering.RenderingAdapter;

/** Fixture: absichtlicher Verstoß, die Implementierung der Anwendungsfälle greift direkt auf einen Adapter zu. */
public class ServiceWithAdapter {

    private final RenderingAdapter adapter = new RenderingAdapter();

    public RenderingAdapter adapter() {
        return adapter;
    }
}
