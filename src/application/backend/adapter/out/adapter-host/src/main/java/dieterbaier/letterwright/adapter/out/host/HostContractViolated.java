package dieterbaier.letterwright.adapter.out.host;

import java.io.Serial;

/**
 * An SPI implementation of the host has violated its contract, for example by returning {@code null} or invalid
 * values. This is an error in the host, not in the user's input.
 */
public final class HostContractViolated extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    HostContractViolated(String message) {
        super(message);
    }

    HostContractViolated(String message, Throwable cause) {
        super(message, cause);
    }
}
