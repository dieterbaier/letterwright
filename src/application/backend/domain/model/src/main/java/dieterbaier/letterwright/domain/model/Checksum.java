package dieterbaier.letterwright.domain.model;

import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;
import org.jmolecules.ddd.annotation.ValueObject;

/** SHA-256-Prüfsumme in Kleinbuchstaben-Hex, etwa eines PDFs oder eines Datenkontexts (ADR-007). */
@ValueObject
public record Checksum(String hex) {

    private static final Pattern SHA256_HEX = Pattern.compile("[0-9a-f]{64}");

    public Checksum {
        if (!SHA256_HEX.matcher(Validate.required(hex, "Die Prüfsumme")).matches()) {
            throw new ValidationException("Keine SHA-256-Prüfsumme in Kleinbuchstaben-Hex: " + hex);
        }
    }

    public static Checksum of(byte[] content) {
        MessageDigest sha256 = sha256();
        return new Checksum(HexFormat.of().formatHex(sha256.digest(Validate.required(content, "Der Inhalt"))));
    }

    static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // Jede Java-Plattform muss SHA-256 anbieten (MessageDigest, "Standard Algorithm Names").
            throw new IllegalStateException("SHA-256 fehlt in dieser Java-Laufzeit", e);
        }
    }
}
