// Gradle-Wurzel des Monorepos. Backend-Module liegen unter backend/ (ADR-009).
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "standardschreiben"

val backendModule = listOf(
    "domaene",
    "anwendung",
    "adapter-persistenz-jdbc",
    "adapter-rendering",
    "adapter-web",
    "spring-boot-starter",
    "architekturtests",
)

backendModule.forEach { modul ->
    include(modul)
    project(":$modul").projectDir = file("backend/$modul")
}
