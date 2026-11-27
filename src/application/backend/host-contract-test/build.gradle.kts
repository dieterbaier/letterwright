plugins {
    id("letterwright.java-module")
}

description = "Proof for ADR-016: a host that includes only the starter implements the SPI and sees nothing else of the library when compiling. Tests only, not published."

dependencies {
    // This is exactly how a host includes the library.
    testImplementation(project(":spring-boot-starter"))
}

tasks.named<Test>("test") {
    // The classpath the host compiles with; the test compiles example sources with it.
    val classpath: FileCollection = files(configurations.named("testCompileClasspath"))
    inputs.files(classpath).withPropertyName("hostClasspath")
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-Dhost.classpath=" + classpath.asPath)
    })
}
