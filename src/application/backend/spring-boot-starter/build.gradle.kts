plugins {
    id("letterwright.java-module")
}

description = "Spring Boot starter: auto-configuration and default adapters for the host application. Exports only the SPI and the Java API (ADR-016, ADR-023)."

dependencies {
    // The host compiles only against the SPI and the Java API. It gets everything else at runtime only (ADR-016, ADR-023).
    api(project(":spi"))
    api(project(":api"))
    implementation(project(":usecases-in"))
    implementation(project(":usecases-out"))
    implementation(project(":usecases-service"))
    implementation(project(":adapter-host"))
    implementation(project(":adapter-persistence-jdbc"))
    implementation(project(":adapter-rendering"))
    implementation(project(":adapter-web"))
    implementation(project(":adapter-api"))
    implementation(libs.spring.boot.autoconfigure)
    // For the default authorization check; the host brings Spring Security anyway (ADR-001).
    implementation(libs.spring.security.core)

    testImplementation(libs.spring.boot.test)
}
