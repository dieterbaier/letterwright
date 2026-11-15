plugins {
    id("standardschreiben.java-modul")
}

description = "Implementierung der Anwendungsfälle: implementiert die eingehenden Ports und nutzt die ausgehenden (ADR-002)."

dependencies {
    api(project(":usecases-in"))
    implementation(project(":usecases-out"))
}
