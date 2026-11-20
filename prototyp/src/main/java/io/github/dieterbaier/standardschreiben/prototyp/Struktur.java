package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.text.PDFMarkedContentExtractor;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;

/**
 * Gibt die Strukturhierarchie (Tags) eines PDFs als Baum aus: Typ, Alternativtext, Sprache und
 * den Text der Inhalte, die das Element per MCID referenziert. Grundlage für die manuelle Stichprobe aus QS-002, die veraPDF nicht ersetzt.
 */
public final class Struktur {

    /** Text je Seite und MCID, wie ihn eine Hilfstechnologie aus dem Tag-Baum bekommt. */
    private static final Map<PDPage, Map<Integer, String>> TEXTE = new HashMap<>();

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
            for (PDPage seite : dokument.getPages()) {
                TEXTE.put(seite, texte(seite));
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
                Map<Integer, String> texte = TEXTE.getOrDefault(e.getPage(), Map.of());
                StringBuilder text = new StringBuilder();
                for (Object mcid : e.getKids()) {
                    if (mcid instanceof Integer i && texte.containsKey(i)) {
                        text.append(texte.get(i));
                    }
                }
                if (!text.isEmpty()) {
                    String t = text.toString().strip();
                    zeile.append("  „").append(t.length() > 70 ? t.substring(0, 67) + "..." : t).append("“");
                }
                System.out.println(zeile);
                ausgeben(e, tiefe + 1);
            }
        }
    }

    /** Text je MCID einer Seite. */
    static Map<Integer, String> texte(PDPage seite) throws IOException {
        PDFMarkedContentExtractor extraktor = new PDFMarkedContentExtractor();
        extraktor.processPage(seite);
        Map<Integer, String> texte = new HashMap<>();
        for (PDMarkedContent mc : extraktor.getMarkedContents()) {
            sammle(mc, texte);
        }
        return texte;
    }

    private static void sammle(PDMarkedContent mc, Map<Integer, String> texte) {
        if (mc.getMCID() >= 0) {
            StringBuilder t = new StringBuilder();
            for (Object inhalt : mc.getContents()) {
                if (inhalt instanceof TextPosition tp) {
                    t.append(tp.getUnicode());
                } else if (inhalt instanceof PDMarkedContent kind) {
                    sammle(kind, texte);
                }
            }
            texte.merge(mc.getMCID(), t.toString(), String::concat);
        }
    }

    private Struktur() {
    }
}
