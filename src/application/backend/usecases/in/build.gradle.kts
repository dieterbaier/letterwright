plugins {
    id("letterwright.java-module")
}

description = "Eingehende Ports: Schnittstellen der Anwendungsfälle, die eingehende Adapter aufrufen (ADR-002)."

dependencies {
    api(project(":domain-model"))
}
