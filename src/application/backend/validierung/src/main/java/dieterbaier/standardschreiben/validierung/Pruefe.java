package dieterbaier.standardschreiben.validierung;

import java.util.Collection;
import org.jspecify.annotations.Nullable;

/**
 * Prüfungen für Konstruktoren und Fabrikmethoden. Jede liefert den geprüften Wert zurück, damit sie sich direkt in einer
 * Zuweisung verwenden lässt.
 *
 * <p>{@link #nichtLeer(String, String)} und {@link #nichtBlank(String, String)} sind verschiedene Verträge: Ein Text aus
 * Leerzeichen ist nicht leer, aber blank.
 */
public final class Pruefe {

    private Pruefe() {
    }

    /** Der Wert muss vorhanden sein. */
    public static <T> T pflicht(@Nullable T wert, String bezeichnung) {
        if (wert == null) {
            throw new Validierungsfehler(bezeichnung + " fehlt");
        }
        return wert;
    }

    /** Der Text muss vorhanden sein und mindestens ein Zeichen haben. Leerraum zählt als Zeichen. */
    public static String nichtLeer(@Nullable String text, String bezeichnung) {
        String vorhanden = pflicht(text, bezeichnung);
        if (vorhanden.isEmpty()) {
            throw new Validierungsfehler(bezeichnung + " darf nicht leer sein");
        }
        return vorhanden;
    }

    /** Der Text muss vorhanden sein und mindestens ein Zeichen haben, das kein Leerraum ist. */
    public static String nichtBlank(@Nullable String text, String bezeichnung) {
        String vorhanden = pflicht(text, bezeichnung);
        if (vorhanden.isBlank()) {
            throw new Validierungsfehler(bezeichnung + " darf nicht leer sein oder nur aus Leerraum bestehen");
        }
        return vorhanden;
    }

    /** Die Zahl muss mindestens {@code minimum} sein. */
    public static int mindestens(int wert, int minimum, String bezeichnung) {
        if (wert < minimum) {
            throw new Validierungsfehler(bezeichnung + " muss mindestens " + minimum + " sein, war " + wert);
        }
        return wert;
    }

    /** Die Sammlung muss vorhanden sein und darf kein fehlendes Element enthalten. */
    public static <C extends Collection<?>> C pflichtOhneLuecken(@Nullable C sammlung, String bezeichnung) {
        C vorhanden = pflicht(sammlung, bezeichnung);
        int position = 0;
        for (Object element : vorhanden) {
            pflicht(element, bezeichnung + "[" + position + "]");
            position++;
        }
        return vorhanden;
    }
}
