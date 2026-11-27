package dieterbaier.letterwright.domain.model.template;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LetterheadTypeTest {

    @Test
    void letterheadWithoutTypeIsBusinessFormB() {
        assertThat(LetterheadType.STANDARD).isEqualTo(LetterheadType.BUSINESS_FORM_B);
    }
}
