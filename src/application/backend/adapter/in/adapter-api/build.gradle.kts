plugins {
    id("letterwright.java-module")
}

description = "Inbound adapter for the Java API: implements api, calls the use cases in the host's transaction and maps between API types and the domain model (ADR-023)."

dependencies {
    implementation(project(":api"))
    implementation(project(":usecases-in"))
    // For the REQUIRED transaction boundary; the host brings the transaction manager (ADR-023).
    implementation(libs.spring.tx)
}
