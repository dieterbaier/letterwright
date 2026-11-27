package dieterbaier.letterwright.api;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Prüfungen der API-Typen. Die API hängt an keinem anderen Modul der Bibliothek (ADR-023) und hat deshalb eigene, kleine
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

    static <T> List<T> withoutGaps(@Nullable List<T> list, String label) {
        List<T> present = value(list, label);
        for (Object element : present) {
            if (element == null) {
                throw new IllegalArgumentException(label + " enthalten einen fehlenden Wert");
            }
        }
        return List.copyOf(present);
    }
}
