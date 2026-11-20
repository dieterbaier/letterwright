package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

/**
 * Baut die Vorschau nach ADR-014 aus demselben HTML, aus dem das PDF entsteht.
 *
 * <p>Das Dokument der Vorschau bekommt im Kopf nur Zusätze, die das Layout nicht ändern: eine
 * Content Security Policy, die Schriften des Hosts als {@code @font-face} mit {@code data:}-URIs,
 * Paged.js mit seiner Konfiguration als Inline-Skripte (in der CSP per Hash erlaubt) und eine
 * Bildschirmdarstellung der Seiten. Die Host-Seite zeigt es in einem iframe mit
 * {@code sandbox="allow-scripts"} ohne {@code allow-same-origin}.
 */
final class Vorschau {

    /** Startet Paged.js und meldet dem Host nach der Paginierung Seitenzahl und Höhe. */
    private static final String KONFIGURATION = """
            window.PagedConfig = {
              auto: true,
              after: (flow) => {
                window.vorschauFertig = true;
                parent.postMessage({
                  typ: "standardschreiben-vorschau",
                  seiten: flow.total,
                  hoehe: document.documentElement.scrollHeight
                }, "*");
              }
            };""";

    /** Seiten auf dem Bildschirm wie Blätter darstellen; im Druck ohne Wirkung. */
    private static final String BILDSCHIRM = """
            @media screen {
              html { background: #e6e6e6; }
              .pagedjs_pages { padding: 8mm 0; }
              .pagedjs_page { background: #fff; margin: 0 auto 8mm; box-shadow: 0 1px 4px rgba(0,0,0,.3); }
            }""";

    record Dateien(Path dokument, Path host) {
    }

    static Dateien erzeuge(String html, String titel, List<Schriften.Schrift> schriften, Path pagedJs,
            Path ausgabe, String name) throws IOException {
        String polyfill = Files.readString(pagedJs, StandardCharsets.UTF_8);
        String csp = String.join("; ",
                "default-src 'none'",
                "script-src '" + sha256(KONFIGURATION) + "' '" + sha256(polyfill) + "'",
                // Paged.js legt zur Laufzeit eigene <style>-Elemente an; ein Hash reicht dafür nicht.
                "style-src 'unsafe-inline'",
                "font-src data:",
                "img-src data:");

        StringBuilder kopf = new StringBuilder();
        kopf.append("<meta http-equiv=\"Content-Security-Policy\" content=\"").append(csp).append("\"/>\n");
        kopf.append("<style>\n").append(fontFace(schriften)).append(BILDSCHIRM).append("\n</style>\n");
        kopf.append("<script>").append(KONFIGURATION).append("</script>\n");
        kopf.append("<script>").append(polyfill).append("</script>\n");

        // Direkt nach <meta charset>, damit die CSP vor allen Ressourcen gilt.
        String marke = "<meta charset=\"UTF-8\"/>";
        int stelle = html.indexOf(marke);
        if (stelle < 0) {
            throw new IllegalStateException("Kein <meta charset=\"UTF-8\"/> im Kopf des Schreibens");
        }
        stelle += marke.length();
        String dokument = html.substring(0, stelle) + "\n" + kopf + html.substring(stelle);

        Path dokumentDatei = ausgabe.resolve(name + "-vorschau-dokument.html");
        Files.writeString(dokumentDatei, dokument, StandardCharsets.UTF_8);
        Path hostDatei = ausgabe.resolve(name + "-vorschau.html");
        Files.writeString(hostDatei, host(dokument, titel), StandardCharsets.UTF_8);
        return new Dateien(dokumentDatei, hostDatei);
    }

    private static String fontFace(List<Schriften.Schrift> schriften) throws IOException {
        StringBuilder css = new StringBuilder();
        for (Schriften.Schrift s : schriften) {
            String typ = s.datei().toString().toLowerCase().endsWith(".otf") ? "font/otf" : "font/ttf";
            css.append("@font-face { font-family: \"").append(s.familie()).append("\"; ")
                    .append("font-weight: ").append(s.gewicht()).append("; ")
                    .append("font-style: ").append(s.kursiv() ? "italic" : "normal").append("; ")
                    .append("src: url(data:").append(typ).append(";base64,")
                    .append(Base64.getEncoder().encodeToString(Files.readAllBytes(s.datei())))
                    .append("); }\n");
        }
        return css.toString();
    }

    /**
     * Host-Seite der Vorschau. Bedienelemente und Statusmeldung liegen außerhalb des iframe; die
     * Nachricht des iframe wird nur angenommen, wenn sie aus dem eigenen iframe kommt und dem
     * festen Schema entspricht. Die Origin hilft nicht, sie ist im Sandbox-Frame {@code null}.
     */
    private static String host(String dokument, String titel) {
        String srcdoc = dokument.replace("&", "&amp;").replace("\"", "&quot;");
        String titelEscaped = titel.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
        return """
                <!DOCTYPE html>
                <html lang="de">
                <head>
                <meta charset="UTF-8"/>
                <title>Vorschau: %1$s</title>
                <style>
                  body { font-family: system-ui, sans-serif; margin: 1rem; }
                  iframe { display: block; width: 230mm; max-width: 100%%; height: 80vh; border: 1px solid #999; }
                </style>
                </head>
                <body>
                <main>
                  <h1>Vorschau</h1>
                  <p id="status" role="status" aria-live="polite">Vorschau wird erstellt …</p>
                  <iframe id="vorschau" title="Vorschau des Schreibens: %1$s" sandbox="allow-scripts"
                          srcdoc="%2$s"></iframe>
                </main>
                <script>
                  const frame = document.getElementById("vorschau");
                  const status = document.getElementById("status");
                  window.addEventListener("message", (event) => {
                    if (event.source !== frame.contentWindow) return;
                    const d = event.data;
                    if (!d || d.typ !== "standardschreiben-vorschau"
                        || !Number.isInteger(d.seiten) || !Number.isFinite(d.hoehe)) return;
                    frame.style.height = Math.ceil(d.hoehe) + "px";
                    status.textContent = "Vorschau bereit: " + d.seiten + (d.seiten === 1 ? " Seite" : " Seiten");
                  });
                </script>
                </body>
                </html>
                """.formatted(titelEscaped, srcdoc);
    }

    private static String sha256(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return "sha256-" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private Vorschau() {
    }
}
