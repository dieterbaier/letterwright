plugins {
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("io.github.openhtmltopdf:openhtmltopdf-pdfbox:1.1.114")
    implementation("org.freemarker:freemarker:2.3.35")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.23.0")
}

application {
    mainClass = "io.github.dieterbaier.standardschreiben.prototyp.Prototyp"
}

// Textpositionen eines PDFs in Millimetern ausgeben, z. B. für den Abgleich mit Word.
tasks.register<JavaExec>("positionen") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.github.dieterbaier.standardschreiben.prototyp.Positionen"
    workingDir = projectDir
}

tasks.named<JavaExec>("run") {
    workingDir = projectDir
}

// Textzeilen zweier PDFs vergleichen: Abweichung von linker Kante und Grundlinie in Millimetern.
tasks.register<JavaExec>("vergleich") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.github.dieterbaier.standardschreiben.prototyp.Vergleich"
    workingDir = projectDir
}
