plugins {
    id("standardschreiben.java-modul")
}

description = "Spring-Boot-Starter: Autokonfiguration und Default-Adapter für die Host-Anwendung."

dependencies {
    api(project(":usecases-in"))
    api(project(":usecases-out"))
    implementation(project(":usecases-service"))
    implementation(project(":adapter-persistenz-jdbc"))
    implementation(project(":adapter-rendering"))
    implementation(project(":adapter-web"))
    implementation(libs.spring.boot.autoconfigure)
}
