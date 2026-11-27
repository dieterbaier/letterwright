package dieterbaier.letterwright.domain.model.letter;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class LetterStateTest {

    @Test
    void aLogicalDeletionKnowsItsTime() {
        Instant at = Instant.parse("2026-11-22T11:00:00Z");

        assertThat(new LetterState.LogicallyDeleted(at).at()).isEqualTo(at);
        rejects(() -> new LetterState.LogicallyDeleted(null), "Zeitpunkt des Löschens");
    }

    @Test
    void statesAreValues() {
        Instant at = Instant.parse("2026-11-22T11:00:00Z");

        assertThat(new LetterState.Visible()).isEqualTo(new LetterState.Visible());
        assertThat(new LetterState.LogicallyDeleted(at)).isEqualTo(new LetterState.LogicallyDeleted(at))
                .isNotEqualTo(new LetterState.Visible());
    }
}
