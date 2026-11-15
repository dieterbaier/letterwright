package io.github.dieterbaier.standardschreiben.archfixture.usecases.out;

import io.github.dieterbaier.standardschreiben.archfixture.adapter.in.web.WebAdapter;

/** Fixture: absichtlicher Verstoß, ein Port kennt einen Adapter. */
public interface AusgehenderPortMitAdapter {

    WebAdapter adapter();
}
