package dieterbaier.letterwright.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class DeletionResultTest {

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void everyResultIsDistinguishable() {
        assertThat(Stream.of(new DeletionResult.Deleted(), new DeletionResult.Blocked("Frist"),
                new DeletionResult.Unknown())
                .map(result -> switch (result) {
                    case DeletionResult.Deleted deleted -> 204;
                    case DeletionResult.Blocked blocked -> 409;
                    case DeletionResult.Unknown unknown -> 404;
                })).containsExactly(204, 409, 404);
    }

    @Test
    void aBlockNamesItsReason() {
        assertThat(new DeletionResult.Blocked("Frist").reason()).isEqualTo("Frist");
        assertThatThrownBy(() -> new DeletionResult.Blocked(" ")).hasMessageContaining("Grund");
    }
}
