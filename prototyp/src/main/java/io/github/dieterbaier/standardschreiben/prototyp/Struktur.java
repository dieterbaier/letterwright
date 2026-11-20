package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;

/**
 * Gibt die Strukturhierarchie (Tags) eines PDFs als Baum aus: Typ, Alternativtext und Sprache.
 * Grundlage für die manuelle Stichprobe aus QS-002, die veraPDF nicht ersetzt.
 */
public final class Struktur {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Aufruf: struktur <datei.pdf>");
            System.exit(2);
        }
        try (PDDocument dokument = Loader.loadPDF(new File(args[0]))) {
            System.out.println("Titel: " + dokument.getDocumentInformation().getTitle()
                    + "; Sprache: " + dokument.getDocumentCatalog().getLanguage());
            PDStructureTreeRoot wurzel = dokument.getDocumentCatalog().getStructureTreeRoot();
            if (wurzel == null) {
                System.out.println("Keine Strukturhierarchie");
                return;
            }
            ausgeben(wurzel, 0);
        }
    }

    private static void ausgeben(PDStructureNode knoten, int tiefe) {
        for (Object kind : knoten.getKids()) {
            if (kind instanceof PDStructureElement e) {
                StringBuilder zeile = new StringBuilder("  ".repeat(tiefe)).append(e.getStructureType());
                if (e.getAlternateDescription() != null) {
                    zeile.append("  Alt=„").append(e.getAlternateDescription()).append("“");
                }
                if (e.getLanguage() != null) {
                    zeile.append("  Lang=").append(e.getLanguage());
                }
                System.out.println(zeile);
                ausgeben(e, tiefe + 1);
            }
        }
    }

    private Struktur() {
    }
}
