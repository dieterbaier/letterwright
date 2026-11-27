plugins {
    id("letterwright.java-module")
}

description = "Outbound ports: implemented only by adapters of the library, no contract with the host (ADR-002, ADR-016)."

dependencies {
    api(project(":domain-model"))
}
