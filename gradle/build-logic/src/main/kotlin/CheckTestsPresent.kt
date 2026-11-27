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
 * Fails if a module has production code but no test execution.
 *
 * JaCoCo skips the coverage verification if no execution data exists. Without this
 * check a module without tests would bypass the coverage gate (ADR-011).
 */
@DisableCachingByDefault(because = "Only checks that the execution data exists, produces no output.")
abstract class CheckTestsPresent : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val productionCode: ConfigurableFileCollection

    @get:Internal
    abstract val executionData: RegularFileProperty

    @get:Input
    abstract val modulePath: Property<String>

    @TaskAction
    fun check() {
        val hasCode = !productionCode.isEmpty
        if (hasCode && !executionData.get().asFile.exists()) {
            throw GradleException(
                "Module ${modulePath.get()} has production code but no tests. " +
                    "The coverage gate (80 % lines and branches) cannot take effect."
            )
        }
    }
}
