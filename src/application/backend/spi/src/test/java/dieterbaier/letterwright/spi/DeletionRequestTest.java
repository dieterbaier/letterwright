package dieterbaier.letterwright.spi;

import static dieterbaier.letterwright.spi.Checks.rejects;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DeletionRequestTest {

    private static final BusinessReference CUSTOMER = new BusinessReference("Kunde", "4711");
    private static final Instant CREATED = Instant.parse("2026-11-21T10:00:00Z");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void theUserIdRejectsMissingEmptyAndBlankValues(String value) {
        rejects(() -> new DeletionRequest(value, UUID.randomUUID(), CUSTOMER, CREATED), "Die Benutzerkennung");
    }

    @Test
    void letterReferenceAndCreationTimeAreRequired() {
        rejects(() -> new DeletionRequest("erika", null, CUSTOMER, CREATED), "Die ID des Schreibens");
        rejects(() -> new DeletionRequest("erika", UUID.randomUUID(), null, CREATED), "Der fachliche Bezug");
        rejects(() -> new DeletionRequest("erika", UUID.randomUUID(), CUSTOMER, null), "Der Erstellungszeitpunkt");
    }
}
