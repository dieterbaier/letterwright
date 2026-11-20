package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Gegenprobe zu {@link Vergleich}: Erkennt der Vergleich Abweichungen, die QS-004 verletzen? Aus
 * einem PDF entstehen Kandidaten mit bekanntem Fehler, die der Vergleich als verletzt melden muss.
 * Als Positivprobe muss das PDF gegen sich selbst als erfüllt gelten. Dazu Proben für den
 * Nachweis der getaggten Fußzeile ({@link FusszeileTaggen#pruefe}).
 */
public final class Gegenprobe {

    interface Veraenderung {
        void anwenden(PDDocument kandidat) throws IOException;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Aufruf: gegenprobe <schreiben.pdf>");
            System.exit(2);
        }
        File datei = new File(args[0]);
        int falsch = 0;
        falsch += probe(datei, "unverändert", true, d -> { });
        falsch += probe(datei, "zusätzliche leere Seite", false,
                d -> d.addPage(new PDPage(d.getPage(0).getMediaBox())));
        falsch += probe(datei, "Kopie der letzten Seite angehängt", false,
                d -> d.importPage(d.getPage(d.getNumberOfPages() - 1)));
        falsch += probe(datei, "zusätzliche Zeile am Ende der Seite 1", false, d -> {
            PDPage seite = d.getPage(0);
            try (PDPageContentStream text = new PDPageContentStream(d, seite, PDPageContentStream.AppendMode.APPEND, true)) {
                text.beginText();
                text.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                // 30 mm vom linken, 5 mm vom unteren Rand: unter allem anderen Text der Seite
                text.newLineAtOffset(85, 14);
                text.showText("Zusaetzliche Zeile");
                text.endText();
            }
        });
        // Nachweis der getaggten Fußzeile (FusszeileTaggen.pruefe): richtig muss bestehen,
        // jede Abweichung muss scheitern.
        java.util.List<String> fuss = java.util.List.of("Öffnungszeiten: 09:00 – 15:00 Uhr", "Homepage: www.superfirma.de");
        falsch += fussprobe(datei, "Fußzeile wie erwartet", true, fuss, 2);
        falsch += fussprobe(datei, "Fußzeile in falscher Reihenfolge", false, fuss.reversed(), 2);
        falsch += fussprobe(datei, "Fußzeile nur teilweise getaggt", false, fuss, 1);
        falsch += fussprobe(datei, "zusätzlicher Block im unteren Rand", false, fuss, 3);
        falsch += fussprobe(datei, "erwarteter Text fehlt", false,
                java.util.List.of(fuss.get(0), fuss.get(1), "Telefon: 0000"), 3);
        System.out.println(falsch == 0 ? "Gegenprobe bestanden." : falsch + " Proben falsch bewertet.");
        System.exit(falsch == 0 ? 0 : 1);
    }

    private static int probe(File datei, String name, boolean erwartet, Veraenderung veraenderung) throws IOException {
        try (PDDocument referenz = Loader.loadPDF(datei); PDDocument kandidat = Loader.loadPDF(datei)) {
            veraenderung.anwenden(kandidat);
            PrintStream aus = System.out;
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            boolean ergebnis;
            try {
                ergebnis = Vergleich.pruefe(Vergleich.lies(referenz), Vergleich.lies(kandidat));
            } finally {
                System.setOut(aus);
            }
            boolean richtig = ergebnis == erwartet;
            System.out.printf("%-36s erwartet %-8s ergebnis %-8s %s%n", name,
                    erwartet ? "erfüllt" : "verletzt", ergebnis ? "erfüllt" : "verletzt", richtig ? "ok" : "FALSCH");
            return richtig ? 0 : 1;
        }
    }

    private static int fussprobe(File datei, String name, boolean erwartet, java.util.List<String> texte, int getaggt) {
        boolean ergebnis;
        try {
            FusszeileTaggen.pruefe(datei.toPath(), texte, getaggt);
            ergebnis = true;
        } catch (IllegalStateException | IOException e) {
            ergebnis = false;
        }
        boolean richtig = ergebnis == erwartet;
        System.out.printf("%-36s erwartet %-8s ergebnis %-8s %s%n", name,
                erwartet ? "bestanden" : "Abbruch", ergebnis ? "bestanden" : "Abbruch", richtig ? "ok" : "FALSCH");
        return richtig ? 0 : 1;
    }

    private Gegenprobe() {
    }
}
