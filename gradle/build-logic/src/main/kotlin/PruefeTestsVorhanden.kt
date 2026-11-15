import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Schlägt fehl, wenn ein Modul Produktionscode, aber keine Testausführung hat.
 *
 * JaCoCo überspringt die Abdeckungsprüfung, wenn keine Ausführungsdaten existieren. Ohne diese
 * Prüfung umginge ein Modul ohne Tests das Abdeckungs-Gate (ADR-011).
 */
@DisableCachingByDefault(because = "Prüft nur die Existenz der Ausführungsdaten, erzeugt keine Ausgabe.")
abstract class PruefeTestsVorhanden : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val produktionsCode: ConfigurableFileCollection

    @get:Internal
    abstract val ausfuehrungsDaten: RegularFileProperty

    @get:Input
    abstract val modulPfad: Property<String>

    @TaskAction
    fun pruefe() {
        val hatCode = !produktionsCode.isEmpty
        if (hatCode && !ausfuehrungsDaten.get().asFile.exists()) {
            throw GradleException(
                "Modul ${modulPfad.get()} hat Produktionscode, aber keine Tests. " +
                    "Das Abdeckungs-Gate (80 % Zeilen und Zweige) kann nicht greifen."
            )
        }
    }
}
