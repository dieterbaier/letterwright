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
 * Weist nach, dass das Null-Gate greift (ADR-019). Kompiliert jede Probe einzeln mit genau den Argumenten des
 * Produktionscodes und erwartet das Ergebnis, das ihre erste Zeile nennt: `// erwartet: kompiliert` oder
 * `// erwartet: <Text der Fehlermeldung>`.
 *
 * Ohne diesen Nachweis bliebe der Build auch grün, wenn NullAway still abgeschaltet wäre, etwa weil ein Paket keine
 * `@NullMarked`-Markierung hat oder eine Option nicht mehr wirkt.
 */
@DisableCachingByDefault(because = "Prüft nur, ob die Proben wie erwartet kompilieren, erzeugt keine Ausgabe.")
abstract class PruefeNullVertrag : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val proben: DirectoryProperty

    @get:Input
    abstract val javac: Property<String>

    @get:Input
    abstract val jvmArgumente: ListProperty<String>

    @get:Input
    abstract val compilerArgumente: ListProperty<String>

    @get:Input
    abstract val release: Property<Int>

    @get:Classpath
    abstract val prozessorPfad: ConfigurableFileCollection

    @get:Classpath
    abstract val klassenPfad: ConfigurableFileCollection

    @TaskAction
    fun pruefe() {
        val dateien = proben.get().asFile.listFiles { datei -> datei.name.endsWith(".java") }.orEmpty().sorted()
        if (dateien.isEmpty()) {
            throw GradleException("Keine Proben in ${proben.get().asFile}")
        }
        val abweichungen = dateien.mapNotNull { probe ->
            val erwartet = probe.readLines().first().removePrefix("// erwartet:").trim()
            val (kompiliert, ausgabe) = kompiliere(probe)
            when {
                erwartet == "kompiliert" && !kompiliert -> "${probe.name}: sollte kompilieren, aber\n$ausgabe"
                erwartet != "kompiliert" && kompiliert -> "${probe.name}: sollte mit '$erwartet' scheitern, kompiliert aber"
                erwartet != "kompiliert" && !ausgabe.contains(erwartet) ->
                    "${probe.name}: sollte mit '$erwartet' scheitern, scheitert aber anders:\n$ausgabe"
                else -> null
            }
        }
        if (abweichungen.isNotEmpty()) {
            throw GradleException("Das Null-Gate greift nicht wie erwartet:\n" + abweichungen.joinToString("\n"))
        }
    }

    private fun kompiliere(probe: java.io.File): Pair<Boolean, String> {
        val ziel = Files.createTempDirectory("null-probe").toFile()
        try {
            val befehl = buildList {
                add(javac.get())
                jvmArgumente.get().forEach { add("-J$it") }
                addAll(compilerArgumente.get())
                addAll(listOf("--release", release.get().toString()))
                addAll(listOf("-processorpath", prozessorPfad.asPath))
                addAll(listOf("-classpath", klassenPfad.asPath))
                addAll(listOf("-d", ziel.absolutePath, probe.absolutePath))
            }
            val prozess = ProcessBuilder(befehl).redirectErrorStream(true).start()
            val ausgabe = prozess.inputStream.bufferedReader().readText()
            return (prozess.waitFor() == 0) to ausgabe
        } finally {
            ziel.deleteRecursively()
        }
    }
}
