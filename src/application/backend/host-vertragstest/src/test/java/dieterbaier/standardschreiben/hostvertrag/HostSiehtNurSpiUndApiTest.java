package dieterbaier.standardschreiben.hostvertrag;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.standardschreiben.api.FachlicherBezug;
import dieterbaier.standardschreiben.api.GespeichertesSchreiben;
import dieterbaier.standardschreiben.api.Speicherergebnis;
import dieterbaier.standardschreiben.api.Standardschreibenversion;
import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Datenanfrage;
import dieterbaier.standardschreiben.spi.SchreibenAktion;
import dieterbaier.standardschreiben.spi.Standardschreibenkennung;
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
class HostSiehtNurSpiUndApiTest {

    @TempDir
    Path ausgabe;

    @Test
    void derBeispielHostImplementiertAlleDreiSchnittstellen() {
        var host = new BeispielHost();
        var kunde = new dieterbaier.standardschreiben.spi.FachlicherBezug("Kunde", "4711");
        var mahnung = new Standardschreibenkennung(UUID.randomUUID(), "Mahnung");

        assertThat(host.datenkontext(new Datenanfrage("erika", kunde, mahnung)).werte())
                .isEqualTo(Map.of("kunde", "4711"));
        assertThat(host.istErlaubt(Berechtigungsanfrage.zumErzeugen("erika", kunde, mahnung))).isTrue();
        assertThat(host.istErlaubt(Berechtigungsanfrage.zuSchreiben("erika", SchreibenAktion.ANSEHEN, kunde))).isTrue();
    }

    @Test
    void derBeispielHostImplementiertDenRueckruf() {
        var host = new BeispielHost();
        var mahnung = new dieterbaier.standardschreiben.spi.Standardschreibenversion(UUID.randomUUID(), 1);
        var erstellt = Instant.parse("2026-11-25T10:00:00Z");

        assertThat(host.angelegt(new dieterbaier.standardschreiben.spi.GespeichertesSchreiben(UUID.randomUUID(),
                new dieterbaier.standardschreiben.spi.FachlicherBezug("Kunde", "4711"), mahnung, "erika", erstellt)))
                .isEqualTo(dieterbaier.standardschreiben.spi.Rueckrufergebnis.abgelehnt(
                        "Die Aufgabe zum Kunden 4711 ist bereits erledigt"));
        assertThat(host.angelegt(new dieterbaier.standardschreiben.spi.GespeichertesSchreiben(UUID.randomUUID(),
                new dieterbaier.standardschreiben.spi.FachlicherBezug("Kunde", "4712"), mahnung, "erika", erstellt)))
                .isEqualTo(dieterbaier.standardschreiben.spi.Rueckrufergebnis.angenommen());
    }

    @Test
    void derBeispielAnwendungsfallSpeichertUeberDieApi() {
        var snapshot = UUID.randomUUID();
        var schreiben = new GespeichertesSchreiben(UUID.randomUUID(), new FachlicherBezug("Kunde", "4711"),
                new Standardschreibenversion(UUID.randomUUID(), 1), "erika", Instant.parse("2026-11-24T10:00:00Z"));
        var anwendungsfall = new BeispielAnwendungsfall((benutzer, id) -> id.equals(snapshot)
                ? new Speicherergebnis.Gespeichert(schreiben)
                : new Speicherergebnis.SnapshotUnbekannt());

        assertThat(anwendungsfall.schliesseAufgabeAb("erika", snapshot)).contains(schreiben.id());
        assertThat(anwendungsfall.schliesseAufgabeAb("erika", UUID.randomUUID())).isEmpty();
    }

    @Test
    void eineQuelleNurMitSpiTypenKompiliert() throws IOException {
        assertThat(kompiliere("import dieterbaier.standardschreiben.spi.FachlicherBezug;")).isEmpty();
    }

    @Test
    void eineQuelleNurMitApiTypenKompiliert() throws IOException {
        assertThat(kompiliere("import dieterbaier.standardschreiben.api.SchreibenSpeichern;")).isEmpty();
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
        "dieterbaier.standardschreiben.adapter.in.api.ApiSchreibenSpeichern",
        "dieterbaier.standardschreiben.usecases.in.SchreibenSpeichern",
        "dieterbaier.standardschreiben.starter.RollenBerechtigungspruefung",
    })
    void eineQuelleMitTypenAusserhalbVonSpiUndApiKompiliertNicht(String typ) throws IOException {
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
