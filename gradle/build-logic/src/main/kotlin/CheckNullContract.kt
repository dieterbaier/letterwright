import java.nio.file.Files
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Proves that the null gate takes effect (ADR-019). Compiles each probe on its own with exactly the arguments of the
 * production code and expects the result its first line names: `// expected: compiles` or
 * `// expected: <text of the error message>`.
 *
 * Without this proof the build would stay green even if NullAway were silently switched off, for example because a
 * package lacks the `@NullMarked` marker or an option no longer takes effect.
 */
@DisableCachingByDefault(because = "Only checks that the probes compile as expected, produces no output.")
abstract class CheckNullContract : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val probes: DirectoryProperty

    @get:Input
    abstract val javac: Property<String>

    @get:Input
    abstract val jvmArguments: ListProperty<String>

    @get:Input
    abstract val compilerArguments: ListProperty<String>

    @get:Input
    abstract val release: Property<Int>

    @get:Classpath
    abstract val processorPath: ConfigurableFileCollection

    @get:Classpath
    abstract val classPath: ConfigurableFileCollection

    @TaskAction
    fun check() {
        val files = probes.get().asFile.listFiles { file -> file.name.endsWith(".java") }.orEmpty().sorted()
        if (files.isEmpty()) {
            throw GradleException("No probes in ${probes.get().asFile}")
        }
        val deviations = files.mapNotNull { probe ->
            val expected = probe.readLines().first().removePrefix("// expected:").trim()
            val (compiled, output) = compile(probe)
            when {
                expected == "compiles" && !compiled -> "${probe.name}: should compile, but\n$output"
                expected != "compiles" && compiled -> "${probe.name}: should fail with '$expected', but compiles"
                expected != "compiles" && !output.contains(expected) ->
                    "${probe.name}: should fail with '$expected', but fails differently:\n$output"
                else -> null
            }
        }
        if (deviations.isNotEmpty()) {
            throw GradleException("The null gate does not take effect as expected:\n" + deviations.joinToString("\n"))
        }
    }

    private fun compile(probe: java.io.File): Pair<Boolean, String> {
        val target = Files.createTempDirectory("null-probe").toFile()
        try {
            val command = buildList {
                add(javac.get())
                jvmArguments.get().forEach { add("-J$it") }
                addAll(compilerArguments.get())
                addAll(listOf("--release", release.get().toString()))
                addAll(listOf("-processorpath", processorPath.asPath))
                addAll(listOf("-classpath", classPath.asPath))
                addAll(listOf("-d", target.absolutePath, probe.absolutePath))
            }
            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            return (process.waitFor() == 0) to output
        } finally {
            target.deleteRecursively()
        }
    }
}
