package io.github.dieterbaier.standardschreiben.prototyp;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PdfAConformance;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/** Erzeugt mit openhtmltopdf aus dem HTML ein PDF/A-2b mit PDF/UA-Tagging. */
final class PdfErzeugung {

    static void erzeuge(String html, String basisUri, List<Schriften.Schrift> schriften, OutputStream out)
            throws IOException {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.usePdfAConformance(PdfAConformance.PDFA_2_B);
        builder.usePdfUaAccessibility(true);
        builder.useColorProfile(ICC_Profile.getInstance(ColorSpace.CS_sRGB).getData());
        for (Schriften.Schrift s : schriften) {
            builder.useFont(s.datei().toFile(), s.familie(), s.gewicht(),
                    s.kursiv() ? FontStyle.ITALIC : FontStyle.NORMAL, true);
        }
        builder.withHtmlContent(html, basisUri);
        builder.toStream(out);
        builder.run();
    }

    private PdfErzeugung() {
    }
}
