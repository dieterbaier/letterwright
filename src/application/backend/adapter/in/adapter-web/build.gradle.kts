plugins {
    id("standardschreiben.java-modul")
}

description = "Eingehender Adapter: REST-Endpunkte (ADR-002). Kennt nur die eingehenden Ports."

dependencies {
    implementation(project(":usecases-in"))
}
