// Gemeinsame Konventionen für alle Backend-Module (ADR-009, ADR-011).
//
// Jedes Modul bindet dieses Plugin ausdrücklich ein. Es gibt keine Konfiguration
// über `subprojects {}` aus dem Root-Projekt.
//
// - Bytecode für Java 21 (--release 21), damit Hosts auf Java 21 die Bibliothek nutzen können.
// - Tests mit JUnit 5, Mockito und AssertJ; Versionen aus der Spring-Boot-BOM.
// - Abdeckungs-Gate: mindestens 80 % Zeilen- UND Zweigabdeckung je Modul, Teil von `check`.
// - Ein Modul mit Produktionscode, aber ohne Tests, schlägt fehl (sonst umginge es das Gate).
// - JSpecify für den Null-Vertrag: Jedes Paket ist in seiner package-info @NullMarked (ADR-015).
//   Die Version steht im Versionskatalog, unabhängig von der Spring-Boot-BOM.
// - -Xpkginfo:always: Jede package-info.java wird zur Klasse, damit ArchUnit auch Pakete ohne
//   weitere Klassen sieht und leere Selektoren auffallen (ADR-015).
// - NullAway setzt den Null-Vertrag beim Kompilieren des Produktionscodes durch (ADR-019).
//   Tests übergeben absichtlich null, um die Selbstvalidierung der Typen zu prüfen.

import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    jacoco
    id("net.ltgt.errorprone")
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
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror", "-parameters", "-Xpkginfo:always"))
}

tasks.named<JavaCompile>("compileJava") {
    // JSpecify-Modus prüft auch die Nullbarkeit von Typargumenten. Auf JDK 21 braucht er
    // -XDaddTypeAnnotationsToSymbol=true; neuere JDKs ignorieren die Option.
    options.compilerArgs.add("-XDaddTypeAnnotationsToSymbol=true")
    options.errorprone {
        check("NullAway", CheckSeverity.ERROR)
        check("RequireExplicitNullMarking", CheckSeverity.ERROR)
        option("NullAway:OnlyNullMarked", "true")
        option("NullAway:JSpecifyMode", "true")
    }
}

tasks.named<JavaCompile>("compileTestJava") {
    options.errorprone.enabled = false
}

dependencies {
    val bom = platform(bibliothek("spring-boot-dependencies"))
    api(bom)
    api(bibliothek("jspecify"))
    errorprone(bibliothek("errorprone-core"))
    errorprone(bibliothek("nullaway"))
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
