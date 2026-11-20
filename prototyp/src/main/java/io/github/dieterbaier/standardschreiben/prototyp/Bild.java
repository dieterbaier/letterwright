package io.github.dieterbaier.standardschreiben.prototyp;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/** Rendert jede Seite eines PDFs als PNG neben das PDF: {@code <name>-s<seite>.png}, 100 dpi. */
public final class Bild {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Aufruf: bild <datei.pdf>");
            System.exit(2);
        }
        File pdf = new File(args[0]);
        String basis = pdf.getPath().replaceFirst("\\.pdf$", "");
        try (PDDocument dokument = Loader.loadPDF(pdf)) {
            PDFRenderer renderer = new PDFRenderer(dokument);
            for (int i = 0; i < dokument.getNumberOfPages(); i++) {
                File png = new File(basis + "-s" + (i + 1) + ".png");
                ImageIO.write(renderer.renderImageWithDPI(i, 100), "png", png);
                System.out.println(png);
            }
        }
    }

    private Bild() {
    }
}
