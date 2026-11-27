plugins {
    id("letterwright.java-module")
}

description = "Nachweis für ADR-016: Ein Host, der nur den Starter einbindet, implementiert das SPI und sieht beim Kompilieren nichts anderes von der Bibliothek. Nur Tests, wird nicht veröffentlicht."

dependencies {
    // Genau so bindet ein Host die Bibliothek ein.
    testImplementation(project(":spring-boot-starter"))
}

tasks.named<Test>("test") {
    // Der Klassenpfad, mit dem der Host kompiliert; der Test kompiliert damit Beispielquellen.
    val klassenpfad: FileCollection = files(configurations.named("testCompileClasspath"))
    inputs.files(klassenpfad).withPropertyName("hostKlassenpfad")
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-Dhost.classpath=" + klassenpfad.asPath)
    })
}
