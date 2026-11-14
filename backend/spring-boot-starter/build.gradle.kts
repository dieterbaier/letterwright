description = "Spring-Boot-Starter: Autokonfiguration und Default-Adapter für die Host-Anwendung."

dependencies {
    api(project(":anwendung"))
    implementation(project(":adapter-persistenz-jdbc"))
    implementation(project(":adapter-rendering"))
    implementation(project(":adapter-web"))
    implementation(libs.spring.boot.autoconfigure)
}
