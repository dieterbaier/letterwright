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

rootProject.name = "letterwright"

// Projektname (zugleich Artefakt-ID) -> Verzeichnis. Die Projekte sind flach benannt,
// damit die Gruppenordner (adapter/in, adapter/out, usecases) keine eigenen Gradle-Projekte werden.
val backendModule = mapOf(
    "validation" to "validation",
    "domain-model" to "domain/model",
    "domain-service" to "domain/service",
    "usecases-in" to "usecases/in",
    "usecases-out" to "usecases/out",
    "usecases-service" to "usecases/service",
    "spi" to "spi",
    "api" to "api",
    "adapter-web" to "adapter/in/adapter-web",
    "adapter-api" to "adapter/in/adapter-api",
    "adapter-rendering" to "adapter/out/adapter-rendering",
    "adapter-persistence-jdbc" to "adapter/out/adapter-persistence-jdbc",
    "adapter-host" to "adapter/out/adapter-host",
    "spring-boot-starter" to "spring-boot-starter",
    "architecture-tests" to "architecture-tests",
    "host-contract-test" to "host-contract-test",
)

backendModule.forEach { (modul, verzeichnis) ->
    include(modul)
    project(":$modul").projectDir = file("src/application/backend/$verzeichnis")
}
