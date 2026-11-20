package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

/**
 * Der Schriftordner des Hosts (ADR-017): einzige Schriftquelle für PDF und Vorschau. Familie,
 * Gewicht und Stil stehen in der Schriftdatei selbst und werden nicht aus dem Dateinamen geraten.
 */
final class Schriften {

    record Schrift(Path datei, String familie, int gewicht, boolean kursiv) {
    }

    static List<Schrift> lies(Path ordner) throws IOException {
        if (!Files.isDirectory(ordner)) {
            throw new IOException("Schriftordner fehlt: " + ordner.toAbsolutePath());
        }
        List<Path> dateien;
        try (Stream<Path> s = Files.list(ordner)) {
            dateien = s.filter(p -> p.getFileName().toString().toLowerCase().matches(".*\\.(ttf|otf)"))
                    .sorted().toList();
        }
        List<Schrift> schriften = new ArrayList<>();
        for (Path datei : dateien) {
            try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(datei.toFile()))) {
                OS2WindowsMetricsTable os2 = ttf.getOS2Windows();
                schriften.add(new Schrift(datei,
                        ttf.getNaming().getFontFamily(),
                        os2 == null ? 400 : os2.getWeightClass(),
                        os2 != null && (os2.getFsSelection() & 1) != 0));
            }
        }
        return schriften;
    }

    private Schriften() {
    }
}
