package dieterbaier.standardschreiben.spi;

import org.jspecify.annotations.Nullable;

/**
 * Prüfungen der SPI-Typen. Das SPI hängt an keinem anderen Modul der Bibliothek (ADR-016) und hat deshalb eigene, kleine
 * Prüfungen. Fehlervertrag wie in der Bibliothek: {@link IllegalArgumentException} mit Attributnamen, nie eine
 * {@link NullPointerException}.
 */
final class Pflicht {

    private Pflicht() {
    }

    static <T> T wert(@Nullable T wert, String bezeichnung) {
        if (wert == null) {
            throw new IllegalArgumentException(bezeichnung + " fehlt");
        }
        return wert;
    }

    static String text(@Nullable String text, String bezeichnung) {
        if (wert(text, bezeichnung).isBlank()) {
            throw new IllegalArgumentException(bezeichnung + " darf nicht leer sein oder nur aus Leerraum bestehen");
        }
        return text;
    }
}
