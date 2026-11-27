// Shared conventions for all backend modules (ADR-009, ADR-011).
//
// Every module applies this plugin explicitly. There is no configuration
// via `subprojects {}` from the root project.
//
// - Bytecode for Java 21 (--release 21), so that hosts on Java 21 can use the library.
// - Tests with JUnit 5, Mockito and AssertJ; versions from the Spring Boot BOM.
// - Coverage gate: at least 80 % line AND branch coverage per module, part of `check`.
// - A module with production code but without tests fails (otherwise it would bypass the gate).
// - JSpecify for the null contract: every package is @NullMarked in its package-info (ADR-015).
//   The version is in the version catalog, independent of the Spring Boot BOM.
// - -Xpkginfo:always: every package-info.java becomes a class, so that ArchUnit also sees packages
//   without further classes and empty selectors stand out (ADR-015).
// - NullAway enforces the null contract when compiling the production code (ADR-019).
//   Tests pass null on purpose to check the self-validation of the types.

import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    jacoco
    id("net.ltgt.errorprone")
}

val catalog = the<VersionCatalogsExtension>().named("libs")
fun library(alias: String) = catalog.findLibrary(alias).get()
val minimumCoverage = "0.80".toBigDecimal()

java {
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror", "-parameters", "-Xpkginfo:always"))
}

tasks.named<JavaCompile>("compileJava") {
    // JSpecify mode also checks the nullability of type arguments. On JDK 21 it needs
    // -XDaddTypeAnnotationsToSymbol=true; newer JDKs ignore the option.
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
    val bom = platform(library("spring-boot-dependencies"))
    api(bom)
    api(library("jspecify"))
    errorprone(library("errorprone-core"))
    errorprone(library("nullaway"))
    testImplementation(bom)
    testImplementation(library("junit-jupiter"))
    testImplementation(library("mockito-junit-jupiter"))
    testImplementation(library("assertj-core"))
    testRuntimeOnly(library("junit-platform-launcher"))
}

jacoco {
    toolVersion = catalog.findVersion("jacoco").get().requiredVersion
}

val test = tasks.named<Test>("test") {
    useJUnitPlatform()
}

val report = tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(test)
    reports {
        xml.required = true
        html.required = true
    }
}
test.configure { finalizedBy(report) }

val coverage = tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(test)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = minimumCoverage
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = minimumCoverage
            }
        }
    }
}

// JaCoCo skips the verification if no execution data exists.
val checkTestsPresent = tasks.register<CheckTestsPresent>("checkTestsPresent") {
    description = "Fails if a module has production code but no test execution."
    group = "verification"
    dependsOn(test)
    productionCode.from(sourceSets.named("main").map { sources ->
        sources.java.asFileTree.matching { exclude("**/package-info.java") }
    })
    executionData = layout.buildDirectory.file("jacoco/test.exec")
    modulePath = project.path
}

tasks.named("check") {
    dependsOn(coverage, checkTestsPresent)
}
