plugins {
    id("letterwright.java-module")
}

description = "Domain services: business rules across several model objects. Free of frameworks, visible at compile time only to usecases-service (ADR-015)."

dependencies {
    api(project(":domain-model"))
}
