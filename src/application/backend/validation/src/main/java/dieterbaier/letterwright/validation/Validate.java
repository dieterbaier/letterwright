package dieterbaier.letterwright.validation;

import java.util.Collection;
import org.jspecify.annotations.Nullable;

/**
 * Prüfungen für Konstruktoren und Fabrikmethoden. Jede liefert den geprüften Wert zurück, damit sie sich direkt in einer
 * Zuweisung verwenden lässt.
 *
 * <p>{@link #notEmpty(String, String)} und {@link #notBlank(String, String)} sind verschiedene Verträge: Ein Text aus
 * Leerzeichen ist nicht leer, aber blank.
 */
public final class Validate {

    private Validate() {
    }

    public static <T> T required(@Nullable T value, String label) {
        if (value == null) {
            throw new ValidationException(label + " fehlt");
        }
        return value;
    }

    /** Der Text muss vorhanden sein und mindestens ein Zeichen haben. Leerraum zählt als Zeichen. */
    public static String notEmpty(@Nullable String text, String label) {
        String present = required(text, label);
        if (present.isEmpty()) {
            throw new ValidationException(label + " darf nicht leer sein");
        }
        return present;
    }

    /** Der Text muss vorhanden sein und mindestens ein Zeichen haben, das kein Leerraum ist. */
    public static String notBlank(@Nullable String text, String label) {
        String present = required(text, label);
        if (present.isBlank()) {
            throw new ValidationException(label + " darf nicht leer sein oder nur aus Leerraum bestehen");
        }
        return present;
    }

    public static int atLeast(int value, int minimum, String label) {
        if (value < minimum) {
            throw new ValidationException(label + " muss mindestens " + minimum + " sein, war " + value);
        }
        return value;
    }

    /** Die Sammlung muss vorhanden sein und darf kein fehlendes Element enthalten. */
    public static <C extends Collection<?>> C requiredWithoutGaps(@Nullable C collection, String label) {
        C present = required(collection, label);
        int position = 0;
        for (Object element : present) {
            required(element, label + "[" + position + "]");
            position++;
        }
        return present;
    }
}
