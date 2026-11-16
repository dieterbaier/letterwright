plugins {
    id("standardschreiben.java-modul")
}

description = "Ausgehende Ports (SPI): implementiert von der Host-Anwendung oder von ausgehenden Adaptern (ADR-002)."

dependencies {
    api(project(":domaene-modell"))
}
