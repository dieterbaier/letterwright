plugins {
    id("letterwright.java-module")
}

description = "Implementation of the use cases: implements the inbound ports and uses the outbound ones (ADR-002)."

dependencies {
    api(project(":usecases-in"))
    implementation(project(":usecases-out"))
    implementation(project(":domain-service"))
}
