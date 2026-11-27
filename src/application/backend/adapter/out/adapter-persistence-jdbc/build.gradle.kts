plugins {
    id("letterwright.java-module")
}

description = "Ausgehender Adapter: Persistenz mit Spring JDBC und Liquibase (ADR-006). Kennt nur die ausgehenden Ports."

dependencies {
    implementation(project(":usecases-out"))
}
