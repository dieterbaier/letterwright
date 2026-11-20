package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    /** Ergebnis der Auflösung: die Schriften für PDF und Vorschau und die Hinweise für die Administration. */
    record Aufloesung(List<Schrift> schriften, List<String> hinweise) {
    }

    private static final Pattern FAMILIE = Pattern.compile("font-family:\\s*\"([^\"]+)\"");

    /** Familien, die das CSS des Schreibens verlangt (nur die erste, in Anführungszeichen). */
    static Set<String> benoetigt(String html) {
        Set<String> familien = new LinkedHashSet<>();
        Matcher m = FAMILIE.matcher(html);
        while (m.find()) {
            familien.add(m.group(1));
        }
        return familien;
    }

    /**
     * Löst jede benötigte Familie in der Reihenfolge aus ADR-017 auf: Schriftordner des Hosts,
     * sonst die freie Ersatzschrift aus der Zuordnungstabelle, sonst die Standardschrift. Eine
     * Ersatzschrift wird unter dem Familiennamen der Vorlage registriert; das CSS bleibt gleich,
     * und PDF und Vorschau bekommen dieselbe Datei.
     */
    static Aufloesung loese(Set<String> familien, Path hostOrdner, Path ersatzOrdner) throws IOException {
        List<Schrift> host = Files.isDirectory(hostOrdner) ? lies(hostOrdner) : List.of();
        List<Schrift> ersatz = lies(ersatzOrdner);
        Properties zuordnung = new Properties();
        try (Reader r = Files.newBufferedReader(ersatzOrdner.resolve("zuordnung.properties"))) {
            zuordnung.load(r);
        }
        String standard = zuordnung.getProperty("*standard*");
        List<Schrift> ergebnis = new ArrayList<>();
        List<String> hinweise = new ArrayList<>();
        for (String familie : familien) {
            List<Schrift> treffer = host.stream().filter(s -> s.familie().equals(familie)).toList();
            if (!treffer.isEmpty()) {
                ergebnis.addAll(treffer);
                continue;
            }
            String zugeordnet = zuordnung.getProperty(familie);
            String ersatzFamilie = zugeordnet;
            List<Schrift> passend = zugeordnet == null ? List.of()
                    : ersatz.stream().filter(s -> s.familie().equals(zugeordnet)).toList();
            if (passend.isEmpty()) {
                ersatzFamilie = standard;
                String f = ersatzFamilie;
                passend = ersatz.stream().filter(s -> s.familie().equals(f)).toList();
                hinweise.add("Schrift „" + familie + "“ fehlt im Schriftordner und hat keine verfügbare Ersatzschrift; "
                        + "verwendet wird die Standardschrift „" + standard + "“.");
            } else {
                hinweise.add("Schrift „" + familie + "“ fehlt im Schriftordner; verwendet wird die Ersatzschrift „"
                        + ersatzFamilie + "“.");
            }
            if (passend.isEmpty()) {
                throw new IOException("Weder Schrift noch Ersatz für „" + familie + "“ gefunden");
            }
            for (Schrift s : passend) {
                ergebnis.add(new Schrift(s.datei(), familie, s.gewicht(), s.kursiv()));
            }
        }
        return new Aufloesung(ergebnis, hinweise);
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
