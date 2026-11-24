plugins {
    id("standardschreiben.java-modul")
}

description = "Spring-Boot-Starter: Autokonfiguration und Default-Adapter für die Host-Anwendung. Exportiert nur SPI und Java-API (ADR-016, ADR-023)."

dependencies {
    // Der Host kompiliert nur gegen SPI und Java-API. Alles andere bekommt er nur zur Laufzeit (ADR-016, ADR-023).
    api(project(":spi"))
    api(project(":api"))
    implementation(project(":usecases-in"))
    implementation(project(":usecases-out"))
    implementation(project(":usecases-service"))
    implementation(project(":adapter-host"))
    implementation(project(":adapter-persistenz-jdbc"))
    implementation(project(":adapter-rendering"))
    implementation(project(":adapter-web"))
    implementation(project(":adapter-api"))
    implementation(libs.spring.boot.autoconfigure)
    // Für die Default-Berechtigungspruefung; der Host bringt Spring Security ohnehin mit (ADR-001).
    implementation(libs.spring.security.core)

    testImplementation(libs.spring.boot.test)
}
