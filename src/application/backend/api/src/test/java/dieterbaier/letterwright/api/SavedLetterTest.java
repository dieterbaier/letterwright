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
    private final BusinessReference customer = new BusinessReference("Kunde", "4711");
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
        rejects(() -> new SavedLetter(null, customer, reminder, "erika", created), "Die ID des Schreibens");
        rejects(() -> new SavedLetter(id, null, reminder, "erika", created), "Der fachliche Bezug");
        rejects(() -> new SavedLetter(id, customer, null, "erika", created), "Das Standardschreiben");
        rejects(() -> new SavedLetter(id, customer, reminder, "erika", null), "Der Zeitpunkt der Erstellung");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void rejectsAMissingOrBlankUserId(String userId) {
        rejects(() -> new SavedLetter(id, customer, reminder, userId, created), "Die Benutzerkennung");
    }
}
