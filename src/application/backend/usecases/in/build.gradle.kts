plugins {
    id("letterwright.java-module")
}

description = "Inbound ports: interfaces of the use cases that inbound adapters call (ADR-002)."

dependencies {
    api(project(":domain-model"))
}
