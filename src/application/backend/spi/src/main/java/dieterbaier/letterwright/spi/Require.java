package dieterbaier.letterwright.spi;

import org.jspecify.annotations.Nullable;

/**
 * Checks for the SPI types. The SPI depends on no other module of the library (ADR-016) and therefore has its own small
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
}
