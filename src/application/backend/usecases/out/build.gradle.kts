plugins {
    id("standardschreiben.java-modul")
}

description = "Ausgehende Ports: nur von Adaptern der Bibliothek implementiert, kein Vertrag mit dem Host (ADR-002, ADR-016)."

dependencies {
    api(project(":domaene-modell"))
}
