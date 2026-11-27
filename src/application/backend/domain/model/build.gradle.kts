plugins {
    id("letterwright.java-module")
}

description = "Fachmodell: Briefpapier, Standardschreiben, Vorlagenversion, Snapshot, Schreiben. Frei von Frameworks."

dependencies {
    // Die Konstruktoren werfen Validierungsfehler; der Fehlervertrag gehört zur API des Modells.
    api(project(":validation"))
    // Nur Annotationen, die die DDD-Rollen kennzeichnen (ADR-018).
    api(libs.jmolecules.ddd)
}
