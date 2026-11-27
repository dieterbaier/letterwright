package dieterbaier.letterwright.validation;

import java.io.Serial;

/** Ein Wert erfüllt eine Prüfung nicht. Die Meldung nennt das Attribut und die verletzte Regel. */
public final class ValidationException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
