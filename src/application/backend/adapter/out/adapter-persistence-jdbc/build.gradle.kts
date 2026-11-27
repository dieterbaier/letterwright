plugins {
    id("letterwright.java-module")
}

description = "Outbound adapter: persistence with Spring JDBC and Liquibase (ADR-006). Knows only the outbound ports."

dependencies {
    implementation(project(":usecases-out"))
}
