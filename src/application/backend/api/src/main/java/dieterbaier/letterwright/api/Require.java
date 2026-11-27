package dieterbaier.letterwright.api;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Checks for the API types. The API depends on no other module of the library (ADR-023) and therefore has its own small
 * checks. Error contract as in the library: {@link IllegalArgumentException} naming the attribute, never a
 * {@link NullPointerException}.
 */
final class Require {

    private Require() {
    }

    static <T> T value(@Nullable T value, String label) {
        if (value == null) {
            throw new IllegalArgumentException(label + " is missing");
        }
        return value;
    }

    static String text(@Nullable String text, String label) {
        String present = value(text, label);
        if (present.isBlank()) {
            throw new IllegalArgumentException(label + " must not be empty or consist only of whitespace");
        }
        return present;
    }

    static <T> List<T> withoutGaps(@Nullable List<T> list, String label) {
        List<T> present = value(list, label);
        for (Object element : present) {
            if (element == null) {
                throw new IllegalArgumentException(label + " contain a missing value");
            }
        }
        return List.copyOf(present);
    }
}
