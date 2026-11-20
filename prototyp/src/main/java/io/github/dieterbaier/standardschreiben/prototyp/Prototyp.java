package io.github.dieterbaier.standardschreiben.prototyp;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.core.HTMLOutputFormat;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wegwerf-Prototyp für Issue #3: führt Briefpapier, Standardschreiben und Daten zu einem
 * HTML-Dokument zusammen und erzeugt daraus ein PDF.
 *
 * <p>Aufruf: {@code ../gradlew run --args="<schreiben> [schriftenordner]"}. Das Schreiben ist der
 * Name einer Datei unter {@code daten/} ohne Endung, z. B. {@code geschaeftsbrief-einseitig}.
 * Ausgabe nach {@code ausgabe/<schreiben>.html}, {@code .pdf} und als Vorschau
 * {@code ausgabe/<schreiben>-vorschau.html}. Die Vorschau braucht vorher {@code npm install}.
 */
public final class Prototyp {

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Aufruf: run <schreiben> [schriftenordner]");
            System.exit(2);
        }
        String schreiben = args[0];
        Path schriften = Path.of(args.length == 2 ? args[1] : "schriften");
        Path ausgabe = Files.createDirectories(Path.of("ausgabe"));

        List<Schriften.Schrift> schriftliste = Schriften.lies(schriften);
        for (Schriften.Schrift s : schriftliste) {
            System.out.printf("Schrift: %-16s %3d %-7s %s%n", s.familie(), s.gewicht(),
                    s.kursiv() ? "kursiv" : "normal", s.datei().getFileName());
        }

        Path datenDatei = Path.of("daten", schreiben + ".json");
        String html = zusammenfuehren(datenDatei);
        Path htmlDatei = ausgabe.resolve(schreiben + ".html");
        Files.writeString(htmlDatei, html, StandardCharsets.UTF_8);

        Path pdfDatei = ausgabe.resolve(schreiben + ".pdf");
        try (OutputStream out = Files.newOutputStream(pdfDatei)) {
            PdfErzeugung.erzeuge(html, htmlDatei.toAbsolutePath().toUri().toString(), schriftliste, out);
        }
        int fuss = FusszeileTaggen.anwenden(pdfDatei);
        System.out.println("Fußzeile: " + fuss + " Textblock/-blöcke vom Artefakt zu getaggtem Inhalt");

        Vorschau.Dateien vorschau = Vorschau.erzeuge(html, betreff(datenDatei), schriftliste,
                Path.of("node_modules", "pagedjs", "dist", "paged.polyfill.min.js"), ausgabe, schreiben);

        System.out.println("HTML:     " + htmlDatei);
        System.out.println("PDF:      " + pdfDatei);
        System.out.println("Vorschau: " + vorschau.host() + " (Dokument im iframe: " + vorschau.dokument() + ")");
    }

    @SuppressWarnings("unchecked")
    private static String betreff(Path datenDatei) throws IOException {
        Map<String, Object> daten = new ObjectMapper().readValue(datenDatei.toFile(), Map.class);
        return (String) ((Map<String, Object>) daten.get("schreiben")).get("betreff");
    }

    /**
     * Zwei Durchläufe: Erst wird das Standardschreiben mit den Daten zu seinem Inhalt, dann das
     * Briefpapier mit Daten und Inhalt zum vollständigen Dokument.
     */
    static String zusammenfuehren(Path datenDatei) throws IOException, TemplateException {
        @SuppressWarnings("unchecked")
        Map<String, Object> daten = new ObjectMapper().readValue(datenDatei.toFile(), Map.class);
        Configuration cfg = konfiguration();

        String standardschreiben = (String) daten.get("standardschreiben");
        String briefpapier = (String) daten.get("briefpapier");

        Map<String, Object> modell = new HashMap<>(daten);
        modell.put("logo", Logo.alsDataUri(Path.of("..", "src", "examples", "Geschaeftsbrief.docx")));
        modell.put("inhalt", render(cfg, "standardschreiben/" + standardschreiben + ".ftlh", modell));
        return render(cfg, "briefpapier/" + briefpapier + ".ftlh", modell);
    }

    /** Abgesicherte Konfiguration nach ADR-008: HTML-Escaping, keine Klassen aus Vorlagen. */
    private static Configuration konfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassForTemplateLoading(Prototyp.class, "/vorlagen");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);
        cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
        cfg.setAPIBuiltinEnabled(false);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        return cfg;
    }

    private static String render(Configuration cfg, String vorlage, Map<String, Object> modell)
            throws IOException, TemplateException {
        Template template = cfg.getTemplate(vorlage);
        StringWriter out = new StringWriter();
        template.process(modell, out);
        return out.toString();
    }

    private Prototyp() {
    }
}
