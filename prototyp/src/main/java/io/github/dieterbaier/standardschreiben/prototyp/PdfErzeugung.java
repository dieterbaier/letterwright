package io.github.dieterbaier.standardschreiben.prototyp;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PdfAConformance;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

/** Erzeugt mit openhtmltopdf aus dem HTML ein PDF/A-2b mit PDF/UA-Tagging. */
final class PdfErzeugung {

    static void erzeuge(String html, String basisUri, Path schriften, OutputStream out) throws IOException {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.usePdfAConformance(PdfAConformance.PDFA_2_B);
        builder.usePdfUaAccessibility(true);
        builder.useColorProfile(ICC_Profile.getInstance(ColorSpace.CS_sRGB).getData());
        registriereSchriften(builder, schriften);
        builder.withHtmlContent(html, basisUri);
        builder.toStream(out);
        builder.run();
    }

    /**
     * ADR-017: Der Ordner des Hosts ist die einzige Schriftquelle. Familie, Gewicht und Stil
     * stehen in der Schriftdatei selbst und werden nicht aus dem Dateinamen geraten.
     */
    private static void registriereSchriften(PdfRendererBuilder builder, Path ordner) throws IOException {
        if (!Files.isDirectory(ordner)) {
            throw new IOException("Schriftordner fehlt: " + ordner.toAbsolutePath());
        }
        List<Path> dateien;
        try (Stream<Path> s = Files.list(ordner)) {
            dateien = s.filter(p -> p.getFileName().toString().toLowerCase().matches(".*\\.(ttf|otf)"))
                    .sorted().toList();
        }
        for (Path datei : dateien) {
            try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(datei.toFile()))) {
                String familie = ttf.getNaming().getFontFamily();
                OS2WindowsMetricsTable os2 = ttf.getOS2Windows();
                int gewicht = os2 == null ? 400 : os2.getWeightClass();
                boolean kursiv = os2 != null && (os2.getFsSelection() & 1) != 0;
                builder.useFont(datei.toFile(), familie, gewicht, kursiv ? FontStyle.ITALIC : FontStyle.NORMAL, true);
                System.out.printf("Schrift: %-28s %3d %-7s %s%n", familie, gewicht, kursiv ? "kursiv" : "normal",
                        datei.getFileName());
            }
        }
    }

    private PdfErzeugung() {
    }
}
