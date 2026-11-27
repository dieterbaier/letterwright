plugins {
    id("letterwright.java-module")
}

description = "Inbound adapter: REST endpoints (ADR-002). Knows only the inbound ports."

dependencies {
    implementation(project(":usecases-in"))
}
