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
 *
 * <p>Zusätzlich prüft er die Kriterien aus QS-004: gleiche Seitenzahl, gleiche erste und letzte
 * Textzeile je Seite, Abweichung höchstens 1 mm. Er weist auch Kandidatenzeilen ohne Partner in
 * der Referenz aus; sonst bliebe eine zusätzliche Zeile oder Seite im Kandidaten unbemerkt. Ist
 * ein Kriterium verletzt, endet er mit Status 1.
 */
public final class Vergleich {

    record Zeile(int seite, float x, float y, String schrift, float groesse, String text) {
    }

    /** Toleranz aus QS-004 für die Positionsabweichung. */
    static final float TOLERANZ_MM = 1.0f;

    /** Segmente, deren Grundlinien so nah beieinander liegen, bilden eine Textzeile. */
    static final float ZEILE_MM = 0.5f;

    record Pdf(int seiten, List<Zeile> zeilen) {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Aufruf: vergleich <referenz.pdf> <kandidat.pdf>");
            System.exit(2);
        }
        if (!pruefe(lies(args[0]), lies(args[1]))) {
            System.exit(1);
        }
    }

    /** Vergleicht zwei PDFs, gibt das Protokoll aus und meldet, ob QS-004 erfüllt ist. */
    static boolean pruefe(Pdf refPdf, Pdf kandPdf) {
        List<Zeile> referenz = refPdf.zeilen();
        List<Zeile> kandidat = kandPdf.zeilen();
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
        // Gegenrichtung: Kandidatenzeilen ohne Partner in der Referenz
        int ueberzaehlig = 0;
        for (Zeile k : kandidat) {
            if (partner(k, referenz).isEmpty()) {
                ueberzaehlig++;
                System.out.printf(Locale.ROOT, "S%d  ÜBERZÄHLIG         %s%n", k.seite(), kurz(k.text()));
            }
        }

        // QS-004: Seitenzahl und Umbruchstellen
        System.out.printf(Locale.ROOT, "%nSeiten: Referenz %d, Kandidat %d%n", refPdf.seiten(), kandPdf.seiten());
        int umbruch = 0;
        for (int seite = 1; seite <= Math.max(refPdf.seiten(), kandPdf.seiten()); seite++) {
            List<String> r = textzeilen(referenz, seite);
            List<String> k = textzeilen(kandidat, seite);
            umbruch += pruefeRand(seite, "erste", r.isEmpty() ? null : r.getFirst(), k.isEmpty() ? null : k.getFirst());
            umbruch += pruefeRand(seite, "letzte", r.isEmpty() ? null : r.getLast(), k.isEmpty() ? null : k.getLast());
        }

        System.out.printf(Locale.ROOT, "%nGrößte Abweichung: dx=%.1f mm, dy=%.1f mm; fehlende Zeilen: %d; überzählige Zeilen: %d; Textabweichungen: %d%n",
                maxDx, maxDy, fehlend, ueberzaehlig, textAbweichung);
        boolean erfuellt = refPdf.seiten() == kandPdf.seiten() && umbruch == 0 && fehlend == 0 && ueberzaehlig == 0
                && textAbweichung == 0 && maxDx <= TOLERANZ_MM && maxDy <= TOLERANZ_MM;
        System.out.println("QS-004: " + (erfuellt ? "erfüllt" : "VERLETZT"));
        return erfuellt;
    }

    /** Vergleicht die erste oder letzte Textzeile einer Seite; null heißt: Seite fehlt oder ist leer. */
    private static int pruefeRand(int seite, String welche, String referenz, String kandidat) {
        boolean gleich = referenz == null ? kandidat == null : kandidat != null && normal(referenz).equals(normal(kandidat));
        if (gleich) {
            return 0;
        }
        System.out.printf(Locale.ROOT, "S%d  %s Zeile weicht ab: Referenz „%s“, Kandidat „%s“%n", seite, welche,
                referenz == null ? "—" : kurz(referenz), kandidat == null ? "—" : kurz(kandidat));
        return 1;
    }

    /**
     * Textzeilen einer Seite von oben nach unten. Segmente mit fast gleicher Grundlinie werden von
     * links nach rechts zu einer Zeile verbunden, z. B. die beiden Spalten der Fußzeile.
     */
    static List<String> textzeilen(List<Zeile> zeilen, int seite) {
        List<Zeile> s = zeilen.stream().filter(z -> z.seite() == seite)
                .sorted(Comparator.comparingDouble(Zeile::y).thenComparingDouble(Zeile::x)).toList();
        List<String> ergebnis = new ArrayList<>();
        List<Zeile> aktuell = new ArrayList<>();
        for (Zeile z : s) {
            if (!aktuell.isEmpty() && z.y() - aktuell.getFirst().y() > ZEILE_MM) {
                ergebnis.add(verbinde(aktuell));
                aktuell.clear();
            }
            aktuell.add(z);
        }
        if (!aktuell.isEmpty()) {
            ergebnis.add(verbinde(aktuell));
        }
        return ergebnis;
    }

    private static String verbinde(List<Zeile> segmente) {
        return String.join(" ", segmente.stream().sorted(Comparator.comparingDouble(Zeile::x)).map(Zeile::text).toList());
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

    static Pdf lies(String datei) throws IOException {
        try (PDDocument dokument = Loader.loadPDF(new File(datei))) {
            return lies(dokument);
        }
    }

    static Pdf lies(PDDocument dokument) throws IOException {
        List<Zeile> zeilen = new ArrayList<>();
        for (Positionen.Zeile z : Positionen.zeilen(dokument)) {
            zeilen.add(new Zeile(z.seite(), z.x(), z.y(), z.schrift(), z.groesse(), z.text()));
        }
        return new Pdf(dokument.getNumberOfPages(), zeilen);
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
