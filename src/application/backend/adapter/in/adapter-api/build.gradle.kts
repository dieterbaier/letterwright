plugins {
    id("letterwright.java-module")
}

description = "Eingehender Adapter für die Java-API: implementiert api, ruft die Anwendungsfälle in der Transaktion des Hosts und bildet zwischen API-Typen und Domänenmodell ab (ADR-023)."

dependencies {
    implementation(project(":api"))
    implementation(project(":usecases-in"))
    // Für die Transaktionsgrenze REQUIRED; den Transaktionsmanager bringt der Host mit (ADR-023).
    implementation(libs.spring.tx)
}
