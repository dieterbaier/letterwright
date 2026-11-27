plugins {
    id("letterwright.java-module")
}

description = "Outbound adapter: merging, PDF generation and conformance check (ADR-004, ADR-005, ADR-008). Knows only the outbound ports."

dependencies {
    implementation(project(":usecases-out"))
}
