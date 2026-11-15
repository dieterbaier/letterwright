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
    "domaene" to "domaene",
    "usecases-in" to "usecases/in",
    "usecases-out" to "usecases/out",
    "usecases-service" to "usecases/service",
    "adapter-web" to "adapter/in/adapter-web",
    "adapter-rendering" to "adapter/out/adapter-rendering",
    "adapter-persistenz-jdbc" to "adapter/out/adapter-persistenz-jdbc",
    "spring-boot-starter" to "spring-boot-starter",
    "architekturtests" to "architekturtests",
)

backendModule.forEach { (modul, verzeichnis) ->
    include(modul)
    project(":$modul").projectDir = file("src/application/backend/$verzeichnis")
}
