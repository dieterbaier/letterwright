package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Gibt je Textzeile eines PDFs Seite, Lage in Millimetern, Schrift und Text aus.
 *
 * <p>Die Lage ist die linke Kante und die Grundlinie, gemessen von der linken oberen Ecke der
 * Seite. Eine Zeile wird an Lücken über 2 mm in Abschnitte geteilt, damit Texte aus nebeneinander
 * liegenden Rahmen und Tabstopps einzeln verglichen werden.
 */
public final class Positionen {

    private static final float MM_PRO_PT = 25.4f / 72f;

    record Zeile(int seite, float x, float y, String schrift, float groesse, String text) {
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "S%d  x=%6.1f  y=%6.1f  %-28s %5.1fpt  %s",
                    seite, x, y, schrift, groesse, text);
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Aufruf: positionen <datei.pdf>");
            System.exit(2);
        }
        try (PDDocument dokument = Loader.loadPDF(new File(args[0]))) {
            zeilen(dokument).forEach(System.out::println);
        }
    }

    static List<Zeile> zeilen(PDDocument dokument) throws IOException {
        List<Zeile> ausgabe = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> positionen) {
                List<TextPosition> abschnitt = new ArrayList<>();
                TextPosition vorige = null;
                for (TextPosition p : positionen) {
                    if (vorige != null && (p.getXDirAdj() - (vorige.getXDirAdj() + vorige.getWidthDirAdj()) > LUECKE_PT
                            || Math.abs(p.getYDirAdj() - vorige.getYDirAdj()) > 1f)) {
                        abschnitt(abschnitt, getCurrentPageNo(), ausgabe);
                        abschnitt = new ArrayList<>();
                    }
                    abschnitt.add(p);
                    vorige = p;
                }
                abschnitt(abschnitt, getCurrentPageNo(), ausgabe);
            }
        };
        stripper.setSortByPosition(true);
        stripper.getText(dokument);
        return ausgabe;
    }

    /** Lücke, ab der zwei Textstücke einer Zeile als getrennte Abschnitte gelten (Tabstopp, Spalte). */
    private static final float LUECKE_PT = 2f / MM_PRO_PT;

    /** Ein Abschnitt beginnt beim ersten sichtbaren Zeichen; Word meldet sonst die Lage eines Tabulators. */
    private static void abschnitt(List<TextPosition> zeichen, int seite, List<Zeile> ausgabe) {
        int start = 0;
        while (start < zeichen.size() && zeichen.get(start).getUnicode().isBlank()) {
            start++;
        }
        if (start == zeichen.size()) {
            return;
        }
        StringBuilder text = new StringBuilder();
        zeichen.subList(start, zeichen.size()).forEach(p -> text.append(p.getUnicode()));
        TextPosition erste = zeichen.get(start);
        String schrift = erste.getFont().getName();
        ausgabe.add(new Zeile(
                seite,
                erste.getXDirAdj() * MM_PRO_PT,
                erste.getYDirAdj() * MM_PRO_PT,
                schrift == null ? "?" : schrift.replaceFirst("^[A-Z]{6}\\+", ""),
                erste.getYScale(),
                text.toString().strip()));
    }

    private Positionen() {
    }
}
