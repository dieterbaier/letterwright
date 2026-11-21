// Gradle-Wurzel des Monorepos. Die Backend-Module liegen unter src/application/backend (ADR-009).
pluginManagement {
    includeBuild("gradle/build-logic")
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "standardschreiben"

// Projektname (zugleich Artefakt-ID) -> Verzeichnis. Die Projekte sind flach benannt,
// damit die Gruppenordner (adapter/in, adapter/out, usecases) keine eigenen Gradle-Projekte werden.
val backendModule = mapOf(
    "validierung" to "validierung",
    "domaene-modell" to "domaene/modell",
    "domaene-service" to "domaene/service",
    "usecases-in" to "usecases/in",
    "usecases-out" to "usecases/out",
    "usecases-service" to "usecases/service",
    "spi" to "spi",
    "adapter-web" to "adapter/in/adapter-web",
    "adapter-rendering" to "adapter/out/adapter-rendering",
    "adapter-persistenz-jdbc" to "adapter/out/adapter-persistenz-jdbc",
    "adapter-host" to "adapter/out/adapter-host",
    "spring-boot-starter" to "spring-boot-starter",
    "architekturtests" to "architekturtests",
    "host-vertragstest" to "host-vertragstest",
)

backendModule.forEach { (modul, verzeichnis) ->
    include(modul)
    project(":$modul").projectDir = file("src/application/backend/$verzeichnis")
}
