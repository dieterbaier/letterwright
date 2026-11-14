package io.github.dieterbaier.standardschreiben.archfixture.anwendung;

import io.github.dieterbaier.standardschreiben.archfixture.adapter.web.WebAdapter;

/** Fixture: absichtlicher Verstoß, die Anwendung greift direkt auf einen Adapter zu. */
public class AnwendungMitAdapter {

    private final WebAdapter adapter = new WebAdapter();

    public WebAdapter adapter() {
        return adapter;
    }
}
