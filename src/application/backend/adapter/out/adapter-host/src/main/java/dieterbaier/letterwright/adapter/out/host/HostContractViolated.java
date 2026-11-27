package dieterbaier.letterwright.adapter.out.host;

import java.io.Serial;

/**
 * Eine SPI-Implementierung des Hosts hat ihren Vertrag verletzt, etwa {@code null} oder ungültige Werte geliefert. Das
 * ist ein Fehler im Host, nicht in der Eingabe des Benutzers.
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
