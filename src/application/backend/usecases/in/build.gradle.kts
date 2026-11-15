plugins {
    id("standardschreiben.java-modul")
}

description = "Eingehende Ports: Schnittstellen der Anwendungsfälle, die eingehende Adapter aufrufen (ADR-002)."

dependencies {
    api(project(":domaene"))
}
