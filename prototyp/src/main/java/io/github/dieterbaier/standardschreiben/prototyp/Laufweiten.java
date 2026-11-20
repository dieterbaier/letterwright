package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

/**
 * Vergleicht Kandidaten für eine Ersatzschrift mit der Originalschrift (ADR-017): Laufweite der
 * Texte, die im Referenzschreiben in dieser Schrift stehen, dazu x-Höhe und Versalhöhe. Eine
 * Laufweite von 100 % heißt: gleiche Zeilenlänge, gleiche Umbrüche.
 *
 * <p>Aufruf: {@code laufweiten <original.ttf> <kandidat.ttf>...}
 */
public final class Laufweiten {

    /** Texte, die im Geschäftsbrief in Arial Narrow stehen. */
    static final List<String> TEXTE = List.of(
            "Öffnungszeiten: 09:00 – 15:00 Uhr", "Homepage: www.superfirma.de", "Seite 2 von 3",
            "Filiale Musterstadt, Postfach 00 00 00, 12345 Musterstadt",
            "Sachgebiet XXX", "(bitte Bezeichnung des Sachgebietes ergänzen)", "Bearbeitet von:ww",
            "(Vorname Nachname)", "Gebäude:", "Musterstr. 1", "12345 Musterstadt", "Telefon:",
            "(bitte ergänzen)", "E-Mail:", "(...)@superfirma.de", "Bankverbindung:",
            "IBAN DE00 0000 0000 0000 0000 00", "BIC XXXXXXXXXXX", "Datum:", "TT.MM.JJJJ",
            "Betreff", "Bezug", "Anlagen", "GZ", "(bei Antwort bitte angeben)");

    public static void main(String[] args) throws IOException {
        Metrik original = miss(new File(args[0]));
        System.out.printf(Locale.ROOT, "%-40s %8s %8s %8s %8s%n", "Schrift", "Laufw.", "max.Abw", "x-Höhe", "Versal");
        System.out.println(original.zeile(original));
        for (int i = 1; i < args.length; i++) {
            System.out.println(miss(new File(args[i])).zeile(original));
        }
    }

    record Metrik(String name, double[] breiten, double xHoehe, double versal) {
        String zeile(Metrik o) {
            double summe = 0;
            double summeO = 0;
            double maxAbw = 0;
            for (int i = 0; i < breiten.length; i++) {
                summe += breiten[i];
                summeO += o.breiten[i];
                maxAbw = Math.max(maxAbw, Math.abs(breiten[i] / o.breiten[i] - 1));
            }
            return String.format(Locale.ROOT, "%-40s %7.1f%% %7.1f%% %7.3f %8.3f", name, 100 * summe / summeO,
                    100 * maxAbw, xHoehe, versal);
        }
    }

    static Metrik miss(File datei) throws IOException {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(datei))) {
            var cmap = ttf.getUnicodeCmapLookup();
            double em = ttf.getUnitsPerEm();
            double[] breiten = new double[TEXTE.size()];
            for (int i = 0; i < TEXTE.size(); i++) {
                double b = 0;
                for (int cp : TEXTE.get(i).codePoints().toArray()) {
                    b += ttf.getAdvanceWidth(cmap.getGlyphId(cp)) / em;
                }
                breiten[i] = b;
            }
            OS2WindowsMetricsTable os2 = ttf.getOS2Windows();
            return new Metrik(ttf.getNaming().getFontFamily() + " " + ttf.getNaming().getFontSubFamily(), breiten,
                    os2.getHeight() / em, os2.getCapHeight() / em);
        }
    }

    private Laufweiten() {
    }
}
