package dieterbaier.letterwright.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class DeletionResultTest {

    /** This is how an inbound adapter tells the results apart; the compiler checks that none is missing. */
    @Test
    void everyResultIsDistinguishable() {
        assertThat(Stream.of(new DeletionResult.Deleted(), new DeletionResult.Blocked("Retention period"),
                new DeletionResult.Unknown())
                .map(result -> switch (result) {
                    case DeletionResult.Deleted deleted -> 204;
                    case DeletionResult.Blocked blocked -> 409;
                    case DeletionResult.Unknown unknown -> 404;
                })).containsExactly(204, 409, 404);
    }

    @Test
    void aBlockNamesItsReason() {
        assertThat(new DeletionResult.Blocked("Retention period").reason()).isEqualTo("Retention period");
        assertThatThrownBy(() -> new DeletionResult.Blocked(" ")).hasMessageContaining("reason");
    }
}
