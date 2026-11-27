plugins {
    id("letterwright.java-module")
}

description = "ArchUnit rules for the hexagonal dependency direction (ADR-002). Tests only, not published."

dependencies {
    testImplementation(project(":validation"))
    testImplementation(project(":domain-model"))
    testImplementation(project(":domain-service"))
    testImplementation(project(":usecases-in"))
    testImplementation(project(":usecases-out"))
    testImplementation(project(":usecases-service"))
    testImplementation(project(":spi"))
    testImplementation(project(":api"))
    testImplementation(project(":adapter-host"))
    testImplementation(project(":adapter-persistence-jdbc"))
    testImplementation(project(":adapter-rendering"))
    testImplementation(project(":adapter-web"))
    testImplementation(project(":adapter-api"))
    testImplementation(project(":spring-boot-starter"))
    testImplementation(libs.archunit.junit5)
    // For the fixtures that use the DDD annotations (ADR-018).
    testImplementation(libs.jmolecules.ddd)
    // Only for the fixture classes that reproduce a rule violation.
    testImplementation(libs.spring.context)
}

// Proof that the null gate takes effect with the arguments of the production code (ADR-019).
val productionCompilation = tasks.named<JavaCompile>("compileJava")
val checkNullContract = tasks.register<CheckNullContract>("checkNullContract") {
    description = "Compiles probes with the configuration of the production code and expects the findings of NullAway."
    group = "verification"
    probes = layout.projectDirectory.dir("src/nullprobe")
    javac = productionCompilation.flatMap { it.javaCompiler }.map { it.executablePath.asFile.absolutePath }
    jvmArguments = productionCompilation.map { task ->
        task.options.forkOptions.jvmArgs.orEmpty() +
            task.options.forkOptions.jvmArgumentProviders.flatMap { it.asArguments() }
    }
    compilerArguments = productionCompilation.map { it.options.allCompilerArgs }
    release = productionCompilation.flatMap { it.options.release }
    processorPath.from(configurations.named("annotationProcessor"))
    classPath.from(configurations.named("compileClasspath"))
}

tasks.named("check") {
    dependsOn(checkNullContract)
}
