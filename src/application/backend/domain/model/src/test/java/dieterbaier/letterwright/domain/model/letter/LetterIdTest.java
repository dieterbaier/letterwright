package dieterbaier.letterwright.domain.model.letter;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class LetterIdTest {

    @Test
    void theLetterIdRequiresAValueAndCanBeRestored() {
        UUID value = UUID.randomUUID();

        rejects(() -> new LetterId(null), "LetterId");
        assertThat(new LetterId(value)).isEqualTo(new LetterId(value));
        assertThat(LetterId.generate()).isNotEqualTo(LetterId.generate());
    }
}
