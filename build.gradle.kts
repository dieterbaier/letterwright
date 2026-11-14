// Gemeinsame Konventionen für alle Backend-Module (ADR-009, ADR-011).
//
// - Bytecode für Java 21 (--release 21), damit Hosts auf Java 21 die Bibliothek nutzen können.
// - Tests mit JUnit 5, Mockito und AssertJ; Versionen aus der Spring-Boot-BOM.
// - Abdeckungs-Gate: mindestens 80 % Zeilen- UND Zweigabdeckung je Modul, Teil von `check`.
// - Ein Modul mit Produktionscode, aber ohne Tests, schlägt fehl (sonst umginge es das Gate).

val katalog = libs
val mindestAbdeckung = "0.80".toBigDecimal()

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "jacoco")

    configure<JavaPluginExtension> {
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release = 21
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror", "-parameters"))
    }

    dependencies {
        val bom = platform(katalog.spring.boot.dependencies)
        "api"(bom)
        "testImplementation"(bom)
        "testImplementation"(katalog.junit.jupiter)
        "testImplementation"(katalog.mockito.junit.jupiter)
        "testImplementation"(katalog.assertj.core)
        "testRuntimeOnly"(katalog.junit.platform.launcher)
    }

    configure<JacocoPluginExtension> {
        toolVersion = katalog.versions.jacoco.get()
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
    val produktionsCode = the<SourceSetContainer>().named("main").map { quellen ->
        quellen.java.asFileTree.matching { exclude("**/package-info.java") }
    }
    val ausfuehrungsDaten = layout.buildDirectory.file("jacoco/test.exec")
    val modulPfad = path

    val pruefeTestsVorhanden = tasks.register("pruefeTestsVorhanden") {
        description = "Schlägt fehl, wenn ein Modul Produktionscode, aber keine Testausführung hat."
        group = "verification"
        dependsOn(test)
        inputs.files(produktionsCode).withPropertyName("produktionsCode")
        doLast {
            val hatCode = !produktionsCode.get().isEmpty
            if (hatCode && !ausfuehrungsDaten.get().asFile.exists()) {
                throw GradleException(
                    "Modul $modulPfad hat Produktionscode, aber keine Tests. " +
                        "Das Abdeckungs-Gate (80 % Zeilen und Zweige) kann nicht greifen."
                )
            }
        }
    }

    tasks.named("check") {
        dependsOn(abdeckung, pruefeTestsVorhanden)
    }
}
