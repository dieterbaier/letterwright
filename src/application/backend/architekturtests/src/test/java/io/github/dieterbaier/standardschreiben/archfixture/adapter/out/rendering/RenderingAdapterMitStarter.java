package io.github.dieterbaier.standardschreiben.archfixture.adapter.out.rendering;

import io.github.dieterbaier.standardschreiben.archfixture.starter.Autokonfiguration;

/** Fixture: absichtlicher Verstoß, ein Adapter hängt vom Starter ab. */
public class RenderingAdapterMitStarter {

    private final Autokonfiguration konfiguration = new Autokonfiguration();

    public Autokonfiguration konfiguration() {
        return konfiguration;
    }
}
