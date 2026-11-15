plugins {
    id("standardschreiben.java-modul")
}

description = "Ausgehender Adapter: Zusammenführen, PDF-Erzeugung und Konformitätsprüfung (ADR-004, ADR-005, ADR-008). Kennt nur die ausgehenden Ports."

dependencies {
    implementation(project(":usecases-out"))
}
