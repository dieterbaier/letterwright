package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;

/**
 * Prüft PDFs mit veraPDF gegen PDF/A-2b (QS-001) und PDF/UA-1 (QS-002).
 *
 * <p>Je Profil und Datei: konform oder nicht, und für jede verletzte Regel die Anzahl und eine
 * Beispielmeldung. Die manuelle Stichprobe aus QS-002 ersetzt das nicht.
 */
public final class Konformitaet {

    private static final List<PDFAFlavour> PROFILE = List.of(PDFAFlavour.PDFA_2_B, PDFAFlavour.PDFUA_1);

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Aufruf: konformitaet <datei.pdf> ...");
            System.exit(2);
        }
        VeraGreenfieldFoundryProvider.initialise();
        boolean alleKonform = true;
        for (String datei : args) {
            for (PDFAFlavour profil : PROFILE) {
                alleKonform &= pruefe(datei, profil);
            }
        }
        System.exit(alleKonform ? 0 : 1);
    }

    private static boolean pruefe(String datei, PDFAFlavour profil) throws Exception {
        try (InputStream in = new FileInputStream(datei);
                PDFAParser parser = Foundries.defaultInstance().createParser(in, profil);
                PDFAValidator validator = Foundries.defaultInstance().createValidator(profil, false)) {
            ValidationResult ergebnis = validator.validate(parser);
            Map<String, Integer> anzahl = new TreeMap<>();
            Map<String, String> beispiel = new TreeMap<>();
            for (TestAssertion a : ergebnis.getTestAssertions()) {
                if (a.getStatus() != TestAssertion.Status.FAILED) {
                    continue;
                }
                String regel = a.getRuleId().getClause() + "-" + a.getRuleId().getTestNumber();
                anzahl.merge(regel, 1, Integer::sum);
                beispiel.putIfAbsent(regel, a.getMessage() + " [" + a.getLocation().getContext() + "]");
            }
            System.out.printf("%-12s %s: %s%n", profil.getId(), datei,
                    ergebnis.isCompliant() ? "konform" : "NICHT konform, " + anzahl.size() + " Regeln verletzt");
            anzahl.forEach((regel, n) -> System.out.printf("    %-12s %4dx  %s%n", regel, n, kuerze(beispiel.get(regel))));
            return ergebnis.isCompliant();
        }
    }

    private static String kuerze(String text) {
        return text.length() > 160 ? text.substring(0, 157) + "..." : text;
    }

    private Konformitaet() {
    }
}
