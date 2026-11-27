plugins {
    id("letterwright.java-module")
}

description = "ArchUnit-Regeln für die hexagonale Abhängigkeitsrichtung (ADR-002). Nur Tests, wird nicht veröffentlicht."

dependencies {
    testImplementation(project(":validation"))
    testImplementation(project(":domain-model"))
    testImplementation(project(":domain-service"))
    testImplementation(project(":usecases-in"))
    testImplementation(project(":usecases-out"))
    testImplementation(project(":usecases-service"))
    testImplementation(project(":spi"))
    testImplementation(project(":api"))
    testImplementation(project(":adapter-host"))
    testImplementation(project(":adapter-persistence-jdbc"))
    testImplementation(project(":adapter-rendering"))
    testImplementation(project(":adapter-web"))
    testImplementation(project(":adapter-api"))
    testImplementation(project(":spring-boot-starter"))
    testImplementation(libs.archunit.junit5)
    // Für die Fixtures, die die DDD-Annotationen verwenden (ADR-018).
    testImplementation(libs.jmolecules.ddd)
    // Nur für die Fixture-Klassen, die einen Regelverstoß nachstellen.
    testImplementation(libs.spring.context)
}

// Nachweis, dass das Null-Gate mit den Argumenten des Produktionscodes greift (ADR-019).
val produktionsKompilierung = tasks.named<JavaCompile>("compileJava")
val pruefeNullVertrag = tasks.register<PruefeNullVertrag>("pruefeNullVertrag") {
    description = "Kompiliert Proben mit der Konfiguration des Produktionscodes und erwartet die Befunde von NullAway."
    group = "verification"
    proben = layout.projectDirectory.dir("src/nullprobe")
    javac = produktionsKompilierung.flatMap { it.javaCompiler }.map { it.executablePath.asFile.absolutePath }
    jvmArgumente = produktionsKompilierung.map { aufgabe ->
        aufgabe.options.forkOptions.jvmArgs.orEmpty() +
            aufgabe.options.forkOptions.jvmArgumentProviders.flatMap { it.asArguments() }
    }
    compilerArgumente = produktionsKompilierung.map { it.options.allCompilerArgs }
    release = produktionsKompilierung.flatMap { it.options.release }
    prozessorPfad.from(configurations.named("annotationProcessor"))
    klassenPfad.from(configurations.named("compileClasspath"))
}

tasks.named("check") {
    dependsOn(pruefeNullVertrag)
}
