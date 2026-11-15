package dieterbaier.standardschreiben.archfixture.usecases.service;

import dieterbaier.standardschreiben.archfixture.adapter.out.rendering.RenderingAdapter;

/** Fixture: absichtlicher Verstoß, die Implementierung der Anwendungsfälle greift direkt auf einen Adapter zu. */
public class ServiceMitAdapter {

    private final RenderingAdapter adapter = new RenderingAdapter();

    public RenderingAdapter adapter() {
        return adapter;
    }
}
