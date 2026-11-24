package dieterbaier.standardschreiben.api;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Prüfungen der API-Typen. Die API hängt an keinem anderen Modul der Bibliothek (ADR-023) und hat deshalb eigene, kleine
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
        String vorhanden = wert(text, bezeichnung);
        if (vorhanden.isBlank()) {
            throw new IllegalArgumentException(bezeichnung + " darf nicht leer sein oder nur aus Leerraum bestehen");
        }
        return vorhanden;
    }

    static <T> List<T> ohneLuecken(@Nullable List<T> liste, String bezeichnung) {
        List<T> vorhanden = wert(liste, bezeichnung);
        for (Object element : vorhanden) {
            if (element == null) {
                throw new IllegalArgumentException(bezeichnung + " enthalten einen fehlenden Wert");
            }
        }
        return List.copyOf(vorhanden);
    }
}
