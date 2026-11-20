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
    implementation("org.verapdf:validation-model:1.30.4")
    implementation("org.jsoup:jsoup:1.24.1")
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

// Jede Seite eines PDFs als PNG ausgeben, für den Sichtvergleich.
tasks.register<JavaExec>("bild") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.github.dieterbaier.standardschreiben.prototyp.Bild"
    workingDir = projectDir
}

// PDF gegen PDF/A-2b (QS-001) und PDF/UA-1 (QS-002) mit veraPDF prüfen.
tasks.register<JavaExec>("konformitaet") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.github.dieterbaier.standardschreiben.prototyp.Konformitaet"
    workingDir = projectDir
}

// Strukturhierarchie (Tags) eines PDFs ausgeben, für die manuelle Stichprobe aus QS-002.
tasks.register<JavaExec>("struktur") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.github.dieterbaier.standardschreiben.prototyp.Struktur"
    workingDir = projectDir
}

tasks.register<JavaExec>("gegenprobe") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.github.dieterbaier.standardschreiben.prototyp.Gegenprobe"
    workingDir = projectDir
}

tasks.register<JavaExec>("laufweiten") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.github.dieterbaier.standardschreiben.prototyp.Laufweiten"
    workingDir = projectDir
}
