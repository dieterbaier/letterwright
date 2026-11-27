package dieterbaier.letterwright.api;

import static dieterbaier.letterwright.api.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class SavedLetterTest {

    private final UUID id = UUID.randomUUID();
    private final BusinessReference customer = new BusinessReference("Customer", "4711");
    private final LetterTemplateVersion reminder = new LetterTemplateVersion(UUID.randomUUID(), 2);
    private final Instant created = Instant.parse("2026-11-24T10:00:00Z");

    @Test
    void carriesTheIdentifiersOfTheLetter() {
        var letter = new SavedLetter(id, customer, reminder, "erika", created);

        assertThat(letter.id()).isEqualTo(id);
        assertThat(letter.reference()).isEqualTo(customer);
        assertThat(letter.letterTemplate()).isEqualTo(reminder);
        assertThat(letter.userId()).isEqualTo("erika");
        assertThat(letter.created()).isEqualTo(created);
    }

    @Test
    void rejectsMissingValues() {
        rejects(() -> new SavedLetter(null, customer, reminder, "erika", created), "The ID of the letter");
        rejects(() -> new SavedLetter(id, null, reminder, "erika", created), "The business reference");
        rejects(() -> new SavedLetter(id, customer, null, "erika", created), "The letter template");
        rejects(() -> new SavedLetter(id, customer, reminder, "erika", null), "The time of creation");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void rejectsAMissingOrBlankUserId(String userId) {
        rejects(() -> new SavedLetter(id, customer, reminder, userId, created), "The user ID");
    }
}
