package dieterbaier.letterwright.validation;

import java.util.Collection;
import org.jspecify.annotations.Nullable;

/**
 * Checks for constructors and factory methods. Each returns the checked value so that it can be used directly in an
 * assignment.
 *
 * <p>{@link #notEmpty(String, String)} and {@link #notBlank(String, String)} are different contracts: a text of
 * spaces is not empty, but blank.
 */
public final class Validate {

    private Validate() {
    }

    public static <T> T required(@Nullable T value, String label) {
        if (value == null) {
            throw new ValidationException(label + " is missing");
        }
        return value;
    }

    /** The text must be present and have at least one character. Whitespace counts as a character. */
    public static String notEmpty(@Nullable String text, String label) {
        String present = required(text, label);
        if (present.isEmpty()) {
            throw new ValidationException(label + " must not be empty");
        }
        return present;
    }

    /** The text must be present and have at least one character that is not whitespace. */
    public static String notBlank(@Nullable String text, String label) {
        String present = required(text, label);
        if (present.isBlank()) {
            throw new ValidationException(label + " must not be empty or consist only of whitespace");
        }
        return present;
    }

    public static int atLeast(int value, int minimum, String label) {
        if (value < minimum) {
            throw new ValidationException(label + " must be at least " + minimum + ", was " + value);
        }
        return value;
    }

    /** The collection must be present and must not contain a missing element. */
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
