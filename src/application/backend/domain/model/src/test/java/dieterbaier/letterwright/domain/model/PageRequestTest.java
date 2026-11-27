package dieterbaier.letterwright.domain.model;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PageRequestTest {

    @Test
    void theRequestKnowsItsOffset() {
        assertThat(new PageRequest(0, 20).offset()).isZero();
        assertThat(new PageRequest(3, 20).offset()).isEqualTo(60);
        assertThat(new PageRequest(Integer.MAX_VALUE, Integer.MAX_VALUE).offset())
                .as("without overflow").isPositive();
    }

    @Test
    void theRequestStartsAtZeroAndHasAtLeastOneEntry() {
        rejects(() -> new PageRequest(-1, 20), "page number");
        rejects(() -> new PageRequest(0, 0), "page size");
    }
}
