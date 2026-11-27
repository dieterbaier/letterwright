plugins {
    id("letterwright.java-module")
}

description = "Outbound adapter to the host application: implements outbound ports against the SPI and maps between SPI types and the domain model (ADR-016)."

dependencies {
    implementation(project(":usecases-out"))
    implementation(project(":spi"))
}
