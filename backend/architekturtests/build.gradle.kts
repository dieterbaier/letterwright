description = "ArchUnit-Regeln für die hexagonale Abhängigkeitsrichtung (ADR-002). Nur Tests, wird nicht veröffentlicht."

dependencies {
    testImplementation(project(":domaene"))
    testImplementation(project(":anwendung"))
    testImplementation(project(":adapter-persistenz-jdbc"))
    testImplementation(project(":adapter-rendering"))
    testImplementation(project(":adapter-web"))
    testImplementation(project(":spring-boot-starter"))
    testImplementation(libs.archunit.junit5)
    // Nur für die Fixture-Klassen, die einen Regelverstoß nachstellen.
    testImplementation(libs.spring.context)
}
