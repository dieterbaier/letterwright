// Gemeinsame Konventionen für alle Backend-Module (ADR-009, ADR-011).
//
// Jedes Modul bindet dieses Plugin ausdrücklich ein. Es gibt keine Konfiguration
// über `subprojects {}` aus dem Root-Projekt.
//
// - Bytecode für Java 21 (--release 21), damit Hosts auf Java 21 die Bibliothek nutzen können.
// - Tests mit JUnit 5, Mockito und AssertJ; Versionen aus der Spring-Boot-BOM.
// - Abdeckungs-Gate: mindestens 80 % Zeilen- UND Zweigabdeckung je Modul, Teil von `check`.
// - Ein Modul mit Produktionscode, aber ohne Tests, schlägt fehl (sonst umginge es das Gate).

plugins {
    `java-library`
    jacoco
}

val katalog = the<VersionCatalogsExtension>().named("libs")
fun bibliothek(alias: String) = katalog.findLibrary(alias).get()
val mindestAbdeckung = "0.80".toBigDecimal()

java {
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror", "-parameters"))
}

dependencies {
    val bom = platform(bibliothek("spring-boot-dependencies"))
    api(bom)
    testImplementation(bom)
    testImplementation(bibliothek("junit-jupiter"))
    testImplementation(bibliothek("mockito-junit-jupiter"))
    testImplementation(bibliothek("assertj-core"))
    testRuntimeOnly(bibliothek("junit-platform-launcher"))
}

jacoco {
    toolVersion = katalog.findVersion("jacoco").get().requiredVersion
}

val test = tasks.named<Test>("test") {
    useJUnitPlatform()
}

val bericht = tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(test)
    reports {
        xml.required = true
        html.required = true
    }
}
test.configure { finalizedBy(bericht) }

val abdeckung = tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(test)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = mindestAbdeckung
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = mindestAbdeckung
            }
        }
    }
}

// JaCoCo überspringt die Prüfung, wenn keine Ausführungsdaten existieren.
val pruefeTestsVorhanden = tasks.register<PruefeTestsVorhanden>("pruefeTestsVorhanden") {
    description = "Schlägt fehl, wenn ein Modul Produktionscode, aber keine Testausführung hat."
    group = "verification"
    dependsOn(test)
    produktionsCode.from(sourceSets.named("main").map { quellen ->
        quellen.java.asFileTree.matching { exclude("**/package-info.java") }
    })
    ausfuehrungsDaten = layout.buildDirectory.file("jacoco/test.exec")
    modulPfad = project.path
}

tasks.named("check") {
    dependsOn(abdeckung, pruefeTestsVorhanden)
}
