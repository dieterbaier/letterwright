plugins {
    id("letterwright.java-module")
}

description = "Domain model: letterhead, letter template, template version, snapshot, letter. Free of frameworks."

dependencies {
    // The constructors throw validation errors; the error contract is part of the model's API.
    api(project(":validation"))
    // Only annotations that mark the DDD roles (ADR-018).
    api(libs.jmolecules.ddd)
}
