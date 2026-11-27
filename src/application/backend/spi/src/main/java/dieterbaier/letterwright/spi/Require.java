package dieterbaier.letterwright.spi;

import org.jspecify.annotations.Nullable;

/**
 * Prüfungen der SPI-Typen. Das SPI hängt an keinem anderen Modul der Bibliothek (ADR-016) und hat deshalb eigene, kleine
 * Prüfungen. Fehlervertrag wie in der Bibliothek: {@link IllegalArgumentException} mit Attributnamen, nie eine
 * {@link NullPointerException}.
 */
final class Require {

    private Require() {
    }

    static <T> T value(@Nullable T value, String label) {
        if (value == null) {
            throw new IllegalArgumentException(label + " fehlt");
        }
        return value;
    }

    static String text(@Nullable String text, String label) {
        String present = value(text, label);
        if (present.isBlank()) {
            throw new IllegalArgumentException(label + " darf nicht leer sein oder nur aus Leerraum bestehen");
        }
        return present;
    }
}
