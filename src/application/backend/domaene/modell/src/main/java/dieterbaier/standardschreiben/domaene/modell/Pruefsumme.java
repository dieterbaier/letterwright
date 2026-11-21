package dieterbaier.standardschreiben.domaene.modell;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/** SHA-256-Prüfsumme in Kleinbuchstaben-Hex, etwa eines PDFs oder eines Datenkontexts (ADR-007). */
public record Pruefsumme(String hex) {

    private static final Pattern SHA256_HEX = Pattern.compile("[0-9a-f]{64}");

    public Pruefsumme {
        if (!SHA256_HEX.matcher(hex).matches()) {
            throw new IllegalArgumentException("Keine SHA-256-Prüfsumme in Kleinbuchstaben-Hex: " + hex);
        }
    }

    /** Berechnet die Prüfsumme über die Bytes. */
    public static Pruefsumme ueber(byte[] inhalt) {
        MessageDigest sha256 = sha256();
        return new Pruefsumme(HexFormat.of().formatHex(sha256.digest(inhalt)));
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
