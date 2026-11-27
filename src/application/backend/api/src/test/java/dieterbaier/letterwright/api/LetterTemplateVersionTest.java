package dieterbaier.letterwright.api;

import static dieterbaier.letterwright.api.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LetterTemplateVersionTest {

    private final UUID reminder = UUID.randomUUID();

    @Test
    void theFirstVersionHasNumberOne() {
        assertThat(new LetterTemplateVersion(reminder, 1).number()).isEqualTo(1);
    }

    @Test
    void rejectsAMissingId() {
        rejects(() -> new LetterTemplateVersion(null, 1), "Die ID des Standardschreibens");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void rejectsANumberBelowOne(int number) {
        rejects(() -> new LetterTemplateVersion(reminder, number), "Die Versionsnummer");
    }
}
