package dieterbaier.letterwright.hostcontract;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.api.BusinessReference;
import dieterbaier.letterwright.api.SavedLetter;
import dieterbaier.letterwright.api.SaveResult;
import dieterbaier.letterwright.api.LetterTemplateVersion;
import dieterbaier.letterwright.spi.AuthorizationRequest;
import dieterbaier.letterwright.spi.DataRequest;
import dieterbaier.letterwright.spi.LetterAction;
import dieterbaier.letterwright.spi.LetterTemplateId;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Schützt den Kompilier-Klassenpfad eines Hosts (ADR-016, ADR-023). Ein Host, der nur den Starter einbindet, darf gegen
 * das SPI und die Java-API kompilieren und gegen nichts anderes aus der Bibliothek. Sonst koppelt er sich unbemerkt an Domäne, Ports oder
 * Adapter, und jedes Refactoring dort bricht ihn.
 *
 * <p>ArchUnit sieht das nicht: Es prüft die Abhängigkeiten im Code der Bibliothek, nicht, was Gradle einem Nutzer des
 * Starters über {@code api} statt {@code implementation} mitgibt. Deshalb kompiliert der Test Beispielquellen mit genau
 * dem Klassenpfad, den Gradle diesem Modul als Host gibt.
 */
class HostSeesOnlySpiAndApiTest {

    @TempDir
    Path output;

    @Test
    void theExampleHostImplementsAllThreeInterfaces() {
        var host = new ExampleHost();
        var customer = new dieterbaier.letterwright.spi.BusinessReference("Kunde", "4711");
        var reminder = new LetterTemplateId(UUID.randomUUID(), "Mahnung");

        assertThat(host.dataContext(new DataRequest("erika", customer, reminder)).values())
                .isEqualTo(Map.of("customer", "4711"));
        assertThat(host.isAllowed(AuthorizationRequest.forCreation("erika", customer, reminder))).isTrue();
        assertThat(host.isAllowed(AuthorizationRequest.forLetter("erika", LetterAction.VIEW, customer))).isTrue();
    }

    @Test
    void theExampleHostImplementsTheCallback() {
        var host = new ExampleHost();
        var reminder = new dieterbaier.letterwright.spi.LetterTemplateVersion(UUID.randomUUID(), 1);
        var created = Instant.parse("2026-11-25T10:00:00Z");

        assertThat(host.created(new dieterbaier.letterwright.spi.SavedLetter(UUID.randomUUID(),
                new dieterbaier.letterwright.spi.BusinessReference("Kunde", "4711"), reminder, "erika", created)))
                .isEqualTo(dieterbaier.letterwright.spi.CallbackResult.rejected(
                        "Die Aufgabe zum Kunden 4711 ist bereits erledigt"));
        assertThat(host.created(new dieterbaier.letterwright.spi.SavedLetter(UUID.randomUUID(),
                new dieterbaier.letterwright.spi.BusinessReference("Kunde", "4712"), reminder, "erika", created)))
                .isEqualTo(dieterbaier.letterwright.spi.CallbackResult.accepted());
    }

    @Test
    void theExampleUseCaseSavesThroughTheApi() {
        var snapshot = UUID.randomUUID();
        var letter = new SavedLetter(UUID.randomUUID(), new BusinessReference("Kunde", "4711"),
                new LetterTemplateVersion(UUID.randomUUID(), 1), "erika", Instant.parse("2026-11-24T10:00:00Z"));
        var useCase = new ExampleUseCase((user, id) -> id.equals(snapshot)
                ? new SaveResult.Saved(letter)
                : new SaveResult.SnapshotUnknown());

        assertThat(useCase.completeTask("erika", snapshot)).contains(letter.id());
        assertThat(useCase.completeTask("erika", UUID.randomUUID())).isEmpty();
    }

    @Test
    void aSourceWithOnlySpiTypesCompiles() throws IOException {
        assertThat(compile("import dieterbaier.letterwright.spi.BusinessReference;")).isEmpty();
    }

    @Test
    void aSourceWithOnlyApiTypesCompiles() throws IOException {
        assertThat(compile("import dieterbaier.letterwright.api.SaveLetter;")).isEmpty();
    }

    /** Die Autokonfiguration bleibt sichtbar, damit ein Host sie ausschließen kann. Sonst nichts vom Starter. */
    @Test
    void onlyTheAutoConfigurationOfTheStarterIsVisible() throws IOException {
        assertThat(compile("import dieterbaier.letterwright.starter.LetterwrightAutoConfiguration;"))
                .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "dieterbaier.letterwright.domain.model.DataContext",
        "dieterbaier.letterwright.validation.Validate",
        "dieterbaier.letterwright.usecases.out.Permissions",
        "dieterbaier.letterwright.adapter.out.host.HostPermissions",
        "dieterbaier.letterwright.adapter.in.api.ApiSaveLetter",
        "dieterbaier.letterwright.usecases.in.SaveLetter",
        "dieterbaier.letterwright.starter.RoleAuthorizationCheck",
    })
    void aSourceWithTypesOutsideSpiAndApiDoesNotCompile(String type) throws IOException {
        assertThat(compile("import " + type + ";")).contains(type.substring(0, type.lastIndexOf('.')));
    }

    /** Kompiliert eine Klasse mit dem Import und liefert die Fehlermeldungen, leer bei Erfolg. */
    private String compile(String importLine) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String source = "package beispiel;\n" + importLine + "\nclass Host {}\n";
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///beispiel/Host.java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean fehlerIgnorieren) {
                return source;
            }
        };
        Files.createDirectories(output);
        var error = new StringWriter();
        boolean erfolg = compiler.getTask(error, null, null,
                List.of("-classpath", System.getProperty("host.classpath"), "-d", output.toString(),
                        "-proc:none"),
                null, List.of(file)).call();
        return erfolg ? "" : error.toString();
    }
}
