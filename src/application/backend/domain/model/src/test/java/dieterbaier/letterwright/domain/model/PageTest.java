package dieterbaier.letterwright.domain.model;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageTest {

    private final PageRequest secondPage = new PageRequest(1, 2);

    @Test
    void thePageCountsThePagesFromTheTotal() {
        assertThat(new Page<>(List.of("c", "d"), 5, secondPage).pageCount()).isEqualTo(3);
        assertThat(new Page<>(List.of("c", "d"), 4, secondPage).pageCount()).isEqualTo(2);
        assertThat(new Page<>(List.of(), 0, new PageRequest(0, 2)).pageCount()).isZero();
        assertThat(new Page<>(List.of(), 2, secondPage).pageCount()).as("direkt hinter dem Ende").isEqualTo(1);
        assertThat(new Page<>(List.of(), 3, new PageRequest(2, 2)).pageCount())
                .as("weiter hinter dem Ende").isEqualTo(2);
    }

    @Test
    void thePageHoldsAnImmutableCopy() {
        List<String> entries = new ArrayList<>(List.of("c"));
        Page<String> page = new Page<>(entries, 3, secondPage);

        entries.add("d");

        assertThat(page.entries()).containsExactly("c");
    }

    @Test
    void thePageMatchesTheRequestAndTheTotal() {
        rejects(() -> new Page<>(null, 0, secondPage), "Einträge der Seite");
        rejects(() -> new Page<>(Arrays.asList("c", null), 4, secondPage), "Einträge der Seite[1]");
        rejects(() -> new Page<>(List.of("c"), 3, null), "PageRequest");
        rejects(() -> new Page<>(List.of("c", "d", "e"), 5, secondPage), "höchstens 2");
        rejects(() -> new Page<>(List.of("c", "d"), 3, secondPage), "Gesamtzahl 3");
        rejects(() -> new Page<>(List.of("e"), 3, new PageRequest(3, 2)), "Gesamtzahl 3");
        rejects(() -> new Page<>(List.of(), -1, secondPage), "nicht negativ");
    }
}
