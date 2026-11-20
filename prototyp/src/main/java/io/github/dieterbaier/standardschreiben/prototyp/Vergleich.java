package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Vergleicht die Textzeilen zweier PDFs: Referenz (z. B. Word-PDF der DOCX-Vorgabe) und Kandidat
 * (z. B. PDF des Prototyps). Gibt je Referenzzeile die Abweichung von linker Kante und Grundlinie
 * in Millimetern aus und am Ende die größte Abweichung.
 *
 * <p>Zwei Zeilen gehören zusammen, wenn sie auf derselben Seite stehen und der eine Text mit dem
 * anderen beginnt. Das fängt den Fall ab, dass ein Renderer Bezeichnung und Wert als eine Zeile
 * schreibt und der andere als zwei. Gibt es mehrere Kandidaten, gewinnt der nächstgelegene.
 */
public final class Vergleich {

    record Zeile(int seite, float x, float y, String schrift, float groesse, String text) {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Aufruf: vergleich <referenz.pdf> <kandidat.pdf>");
            System.exit(2);
        }
        List<Zeile> referenz = lies(args[0]);
        List<Zeile> kandidat = lies(args[1]);
        float maxDx = 0;
        float maxDy = 0;
        int fehlend = 0;
        int textAbweichung = 0;
        for (Zeile r : referenz) {
            Optional<Zeile> k = partner(r, kandidat);
            if (k.isEmpty()) {
                fehlend++;
                System.out.printf(Locale.ROOT, "S%d  FEHLT              %s%n", r.seite(), kurz(r.text()));
                continue;
            }
            float dx = k.get().x() - r.x();
            float dy = k.get().y() - r.y();
            maxDx = Math.max(maxDx, Math.abs(dx));
            maxDy = Math.max(maxDy, Math.abs(dy));
            String schrift = r.schrift().equals(k.get().schrift()) ? "" : "  Schrift " + k.get().schrift();
            // Eine Bezeichnung, die der eine Renderer mit ihrem Wert als einen Abschnitt schreibt,
            // passt als Präfix. Ein umbrochener Text passt genauso; deshalb wird jede Textabweichung
            // ausgewiesen, damit ein Umbruch nicht als Treffer durchgeht.
            String text = normal(r.text()).equals(normal(k.get().text())) ? "" : "  TEXT≠ „" + kurz(k.get().text()) + "“";
            if (!text.isEmpty()) {
                textAbweichung++;
            }
            System.out.printf(Locale.ROOT, "S%d  dx=%+5.1f  dy=%+5.1f  %s%s%s%n", r.seite(), dx, dy, kurz(r.text()), schrift, text);
        }
        System.out.printf(Locale.ROOT, "%nGrößte Abweichung: dx=%.1f mm, dy=%.1f mm; fehlende Zeilen: %d; Textabweichungen: %d%n",
                maxDx, maxDy, fehlend, textAbweichung);
    }

    static Optional<Zeile> partner(Zeile r, List<Zeile> kandidaten) {
        String rt = normal(r.text());
        return kandidaten.stream()
                .filter(k -> k.seite() == r.seite())
                .filter(k -> {
                    String kt = normal(k.text());
                    return !kt.isEmpty() && (rt.startsWith(kt) || kt.startsWith(rt));
                })
                .min(Comparator.comparingDouble(k -> Math.hypot(k.x() - r.x(), k.y() - r.y())));
    }

    static List<Zeile> lies(String datei) throws IOException {
        List<Zeile> zeilen = new ArrayList<>();
        try (PDDocument dokument = Loader.loadPDF(new File(datei))) {
            for (Positionen.Zeile z : Positionen.zeilen(dokument)) {
                zeilen.add(new Zeile(z.seite(), z.x(), z.y(), z.schrift(), z.groesse(), z.text()));
            }
        }
        return zeilen;
    }

    private static String normal(String text) {
        return text.replaceAll("\\s+", "");
    }

    private static String kurz(String text) {
        String t = text.strip().replaceAll("\\s+", " ");
        return t.length() > 60 ? t.substring(0, 57) + "..." : t;
    }

    private Vergleich() {
    }
}
