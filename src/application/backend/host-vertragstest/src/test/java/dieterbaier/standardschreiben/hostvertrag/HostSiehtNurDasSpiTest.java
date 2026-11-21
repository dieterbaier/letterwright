package dieterbaier.standardschreiben.hostvertrag;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.spi.Aktion;
import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Datenanfrage;
import dieterbaier.standardschreiben.spi.FachlicherBezug;
import dieterbaier.standardschreiben.spi.Standardschreibenkennung;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * Ein Host, der nur den Starter einbindet, kompiliert gegen das SPI und gegen nichts anderes aus der Bibliothek
 * (ADR-016). Der Test kompiliert Beispielquellen mit genau dem Klassenpfad, den Gradle diesem Modul gibt.
 */
class HostSiehtNurDasSpiTest {

    @TempDir
    Path ausgabe;

    @Test
    void derBeispielHostImplementiertAlleDreiSchnittstellen() {
        var host = new BeispielHost();
        var kunde = new FachlicherBezug("Kunde", "4711");

        assertThat(host.datenkontext(new Datenanfrage("erika", kunde,
                new Standardschreibenkennung(UUID.randomUUID(), "Mahnung"))).werte())
                .isEqualTo(Map.of("kunde", "4711"));
        assertThat(host.istErlaubt(new Berechtigungsanfrage("erika", Aktion.SCHREIBEN_ERZEUGEN, kunde))).isTrue();
    }

    @Test
    void eineQuelleNurMitSpiTypenKompiliert() throws IOException {
        assertThat(kompiliere("import dieterbaier.standardschreiben.spi.FachlicherBezug;")).isEmpty();
    }

    /** Die Autokonfiguration bleibt sichtbar, damit ein Host sie ausschließen kann. Sonst nichts vom Starter. */
    @Test
    void vomStarterIstNurDieAutokonfigurationSichtbar() throws IOException {
        assertThat(kompiliere("import dieterbaier.standardschreiben.starter.StandardschreibenAutoKonfiguration;"))
                .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "dieterbaier.standardschreiben.domaene.modell.Datenkontext",
        "dieterbaier.standardschreiben.validierung.Pruefe",
        "dieterbaier.standardschreiben.usecases.out.Berechtigungen",
        "dieterbaier.standardschreiben.adapter.out.host.HostBerechtigungen",
        "dieterbaier.standardschreiben.starter.RollenBerechtigungspruefung",
    })
    void eineQuelleMitTypenAusserhalbDesSpiKompiliertNicht(String typ) throws IOException {
        assertThat(kompiliere("import " + typ + ";")).contains(typ.substring(0, typ.lastIndexOf('.')));
    }

    /** Kompiliert eine Klasse mit dem Import und liefert die Fehlermeldungen, leer bei Erfolg. */
    private String kompiliere(String importZeile) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String quelle = "package beispiel;\n" + importZeile + "\nclass Host {}\n";
        JavaFileObject datei = new SimpleJavaFileObject(URI.create("string:///beispiel/Host.java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean fehlerIgnorieren) {
                return quelle;
            }
        };
        Files.createDirectories(ausgabe);
        var fehler = new StringWriter();
        boolean erfolg = compiler.getTask(fehler, null, null,
                List.of("-classpath", System.getProperty("host.klassenpfad"), "-d", ausgabe.toString(),
                        "-proc:none"),
                null, List.of(datei)).call();
        return erfolg ? "" : fehler.toString();
    }
}
