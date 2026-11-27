package dieterbaier.letterwright.spi;

import static dieterbaier.letterwright.spi.Checks.rejects;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DeletionRequestTest {

    private static final BusinessReference CUSTOMER = new BusinessReference("Customer", "4711");
    private static final Instant CREATED = Instant.parse("2026-11-21T10:00:00Z");

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void theUserIdRejectsMissingEmptyAndBlankValues(String value) {
        rejects(() -> new DeletionRequest(value, UUID.randomUUID(), CUSTOMER, CREATED), "The user ID");
    }

    @Test
    void letterReferenceAndCreationTimeAreRequired() {
        rejects(() -> new DeletionRequest("erika", null, CUSTOMER, CREATED), "The ID of the letter");
        rejects(() -> new DeletionRequest("erika", UUID.randomUUID(), null, CREATED), "The business reference");
        rejects(() -> new DeletionRequest("erika", UUID.randomUUID(), CUSTOMER, null), "The creation time");
    }
}
