package dieterbaier.letterwright.validation;

import java.io.Serial;

/** A value fails a check. The message names the attribute and the violated rule. */
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
