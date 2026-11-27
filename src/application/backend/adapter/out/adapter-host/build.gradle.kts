plugins {
    id("letterwright.java-module")
}

description = "Ausgehender Adapter zur Host-Anwendung: implementiert ausgehende Ports gegen das SPI und bildet zwischen SPI-Typen und Domänenmodell ab (ADR-016)."

dependencies {
    implementation(project(":usecases-out"))
    implementation(project(":spi"))
}
