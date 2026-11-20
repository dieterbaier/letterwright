package io.github.dieterbaier.standardschreiben.prototyp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/**
 * Holt das Logo aus der DOCX-Vorgabe, statt eine Kopie einzuchecken, und schneidet es wie Word
 * zu ({@code a:srcRect t="26077" b="26077"}: oben und unten je 26,077 %).
 */
final class Logo {

    static String alsDataUri(Path docx) throws IOException {
        byte[] jpeg;
        try (ZipFile zip = new ZipFile(docx.toFile())) {
            jpeg = zip.getInputStream(zip.getEntry("word/media/image1.jpeg")).readAllBytes();
        }
        BufferedImage bild = ImageIO.read(new ByteArrayInputStream(jpeg));
        int weg = Math.round(bild.getHeight() * 0.26077f);
        BufferedImage zugeschnitten = bild.getSubimage(0, weg, bild.getWidth(), bild.getHeight() - 2 * weg);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(zugeschnitten, "png", png);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png.toByteArray());
    }

    private Logo() {
    }
}
