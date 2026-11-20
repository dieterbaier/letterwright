package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;

/**
 * Nachbearbeitung des PDFs: macht die Fußzeile vom Artefakt zu getaggtem Inhalt.
 *
 * <p>openhtmltopdf legt laufende Elemente ({@code position: running()}) grundsätzlich als
 * Paginierungs-Artefakt ab; eine Einstellung dafür gibt es nicht. Hilfstechnologien lesen die
 * Fußzeile mit Öffnungszeiten und Homepage deshalb nicht vor. Dieser Schritt sucht auf jeder
 * Seite die Artefakt-Abschnitte, deren Text ganz im unteren Seitenrand steht, markiert jeden
 * Textblock darin als {@code /P <</MCID n>>} und hängt je Block ein Strukturelement {@code P} ans
 * Ende des Dokuments. Damit wird die Fußzeile nach dem Brieftext gelesen, wie in der Vorschau.
 *
 * <p>Kopfzeilen wie „Seite x von y“ stehen im oberen Rand und bleiben Artefakte.
 */
final class FusszeileTaggen {

    /** Unterer Seitenrand aller Seiten: 20 mm in Punkt. */
    private static final float UNTERER_RAND_PT = 20f / 25.4f * 72f;

    static int anwenden(Path pdf) throws IOException {
        Path temp = pdf.resolveSibling(pdf.getFileName() + ".tmp");
        int getaggt = 0;
        try (PDDocument dokument = Loader.loadPDF(pdf.toFile())) {
            PDStructureTreeRoot wurzel = dokument.getDocumentCatalog().getStructureTreeRoot();
            PDStructureElement document = (PDStructureElement) wurzel.getKids().getFirst();
            for (PDPage seite : dokument.getPages()) {
                getaggt += seite(dokument, seite, wurzel, document);
            }
            try (OutputStream out = Files.newOutputStream(temp)) {
                dokument.save(out);
            }
        }
        Files.move(temp, pdf, StandardCopyOption.REPLACE_EXISTING);
        return getaggt;
    }

    private static int seite(PDDocument dokument, PDPage seite, PDStructureTreeRoot wurzel,
            PDStructureElement document) throws IOException {
        List<Object> tokens = new ArrayList<>(new PDFStreamParser(seite).parse());
        int naechsteMcid = naechsteMcid(tokens);
        List<Integer> fussAbschnitte = fussAbschnitte(tokens);
        if (fussAbschnitte.isEmpty()) {
            return 0;
        }
        COSArray elementeDerSeite = elementeDerSeite(wurzel, seite.getStructParents());
        // Neue Tokenliste. Jeder Textblock (BT … ET) im Fußabschnitt bekommt eigenen Marked
        // Content und ein eigenes P, sonst flössen die beiden Spalten der Fußzeile ohne Trennung
        // ineinander („UhrHomepage“).
        List<Object> neu = new ArrayList<>();
        List<COSDictionary> markierungen = new ArrayList<>();
        boolean imFuss = false;
        boolean offen = false;
        for (int i = 0; i < tokens.size(); i++) {
            Object t = tokens.get(i);
            if (fussAbschnitte.contains(i + 2)) {
                // /Artifact /PropN BDC des Fußabschnitts: weglassen, die Blöcke werden einzeln markiert
                imFuss = true;
                i += 2;
                continue;
            }
            if (imFuss && t instanceof Operator op) {
                switch (op.getName()) {
                    case "BT" -> {
                        COSDictionary eigenschaften = new COSDictionary();
                        eigenschaften.setInt(COSName.MCID, naechsteMcid++);
                        markierungen.add(eigenschaften);
                        neu.add(COSName.P);
                        neu.add(eigenschaften);
                        neu.add(Operator.getOperator("BDC"));
                        neu.add(t);
                        offen = true;
                        continue;
                    }
                    case "ET" -> {
                        neu.add(t);
                        neu.add(Operator.getOperator("EMC"));
                        offen = false;
                        continue;
                    }
                    case "EMC" -> {
                        // Ende des früheren Artefakts; Inhalt außerhalb der Textblöcke bleibt unmarkiert
                        imFuss = false;
                        if (offen) {
                            throw new IOException("Fußabschnitt endet in einem offenen Textblock");
                        }
                        continue;
                    }
                    default -> { }
                }
            }
            neu.add(t);
        }
        for (COSDictionary eigenschaften : markierungen) {
            int mcid = eigenschaften.getInt(COSName.MCID);
            PDStructureElement p = new PDStructureElement(COSName.P.getName(), document);
            p.setPage(seite);
            p.appendKid(new PDMarkedContent(COSName.P, eigenschaften));
            document.appendKid(p);
            while (elementeDerSeite.size() < mcid) {
                elementeDerSeite.add(org.apache.pdfbox.cos.COSNull.NULL);
            }
            elementeDerSeite.add(p);
        }
        tokens = neu;
        PDStream strom = new PDStream(dokument);
        try (OutputStream out = strom.createOutputStream(COSName.FLATE_DECODE)) {
            new ContentStreamWriter(out).writeTokens(tokens);
        }
        seite.setContents(strom);
        return markierungen.size();
    }

    /** Positionen der BDC-Operatoren von Artefakten, deren Text ganz im unteren Rand steht. */
    private static List<Integer> fussAbschnitte(List<Object> tokens) {
        List<Integer> treffer = new ArrayList<>();
        int bdc = -1;
        int texte = 0;
        boolean imRand = true;
        for (int i = 0; i < tokens.size(); i++) {
            if (!(tokens.get(i) instanceof Operator op)) {
                continue;
            }
            switch (op.getName()) {
                case "BDC" -> {
                    bdc = i >= 2 && COSName.ARTIFACT.equals(tokens.get(i - 2)) ? i : -1;
                    texte = 0;
                    imRand = true;
                }
                case "Tm" -> {
                    if (bdc >= 0 && tokens.get(i - 1) instanceof COSNumber y && y.floatValue() >= UNTERER_RAND_PT) {
                        imRand = false;
                    }
                }
                case "Tj", "TJ", "'", "\"" -> texte++;
                case "EMC" -> {
                    if (bdc >= 0 && texte > 0 && imRand) {
                        treffer.add(bdc);
                    }
                    bdc = -1;
                }
                default -> { }
            }
        }
        return treffer;
    }

    private static int naechsteMcid(List<Object> tokens) {
        int max = -1;
        for (Object t : tokens) {
            if (t instanceof COSDictionary d && d.containsKey(COSName.MCID)) {
                max = Math.max(max, d.getInt(COSName.MCID));
            }
        }
        return max + 1;
    }

    /** Eintrag der Seite im ParentTree: Array der Strukturelemente, Index = MCID. */
    private static COSArray elementeDerSeite(PDStructureTreeRoot wurzel, int schluessel) throws IOException {
        COSArray eintrag = suche(wurzel.getParentTree().getCOSObject(), schluessel);
        if (eintrag == null) {
            throw new IOException("Kein ParentTree-Eintrag für StructParents " + schluessel);
        }
        return eintrag;
    }

    private static COSArray suche(COSDictionary knoten, int schluessel) {
        COSArray nums = knoten.getCOSArray(COSName.NUMS);
        if (nums != null) {
            for (int i = 0; i + 1 < nums.size(); i += 2) {
                if (nums.getObject(i) instanceof COSInteger k && k.intValue() == schluessel) {
                    COSBase wert = nums.getObject(i + 1);
                    return wert instanceof COSArray a ? a : null;
                }
            }
        }
        COSArray kinder = knoten.getCOSArray(COSName.KIDS);
        if (kinder != null) {
            for (int i = 0; i < kinder.size(); i++) {
                COSBase kind = kinder.getObject(i);
                if (kind instanceof COSObject o) {
                    kind = o.getObject();
                }
                if (kind instanceof COSDictionary d) {
                    COSArray treffer = suche(d, schluessel);
                    if (treffer != null) {
                        return treffer;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Nachweis nach dem Taggen: Die letzten Elemente des Strukturbaums sind genau die erwarteten
     * Fußzeilentexte, in dieser Reihenfolge, je als {@code P}, und es wurde nichts anderes getaggt.
     * Fängt einen falschen Treffer (anderer Text im unteren Rand) und eine nur teilweise getaggte
     * Fußzeile ab. Wirft, wenn der Nachweis scheitert.
     */
    static List<String> pruefe(Path pdf, List<String> erwartet, int getaggt) throws IOException {
        if (getaggt != erwartet.size()) {
            throw new IllegalStateException("Fußzeile: " + getaggt + " Textblöcke getaggt, erwartet "
                    + erwartet.size() + " " + erwartet);
        }
        try (PDDocument dokument = Loader.loadPDF(pdf.toFile())) {
            PDStructureTreeRoot wurzel = dokument.getDocumentCatalog().getStructureTreeRoot();
            PDStructureElement document = (PDStructureElement) wurzel.getKids().getFirst();
            List<Object> kinder = document.getKids();
            if (kinder.size() < erwartet.size()) {
                throw new IllegalStateException("Fußzeile: Strukturbaum zu kurz");
            }
            List<String> gefunden = new ArrayList<>();
            List<Object> letzte = kinder.subList(kinder.size() - erwartet.size(), kinder.size());
            for (Object kind : letzte) {
                if (!(kind instanceof PDStructureElement e) || !"P".equals(e.getStructureType())) {
                    throw new IllegalStateException("Fußzeile: Ende des Strukturbaums ist kein P: " + kind);
                }
                var texte = Struktur.texte(e.getPage());
                StringBuilder text = new StringBuilder();
                for (Object mcid : e.getKids()) {
                    if (mcid instanceof Integer i && texte.containsKey(i)) {
                        text.append(texte.get(i));
                    }
                }
                gefunden.add(text.toString());
            }
            for (int i = 0; i < erwartet.size(); i++) {
                if (!normal(gefunden.get(i)).equals(normal(erwartet.get(i)))) {
                    throw new IllegalStateException("Fußzeile: erwartet " + erwartet + ", im Strukturbaum " + gefunden);
                }
            }
            return gefunden;
        }
    }

    private static String normal(String text) {
        return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFKC).replaceAll("\\s+", " ").strip();
    }

    private FusszeileTaggen() {
    }
}
