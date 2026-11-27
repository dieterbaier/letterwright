plugins {
    id("letterwright.java-module")
}

description = "Implementierung der Anwendungsfälle: implementiert die eingehenden Ports und nutzt die ausgehenden (ADR-002)."

dependencies {
    api(project(":usecases-in"))
    implementation(project(":usecases-out"))
    implementation(project(":domain-service"))
}
