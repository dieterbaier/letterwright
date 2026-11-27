package dieterbaier.letterwright.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.Page;
import dieterbaier.letterwright.domain.model.PageRequest;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ListResultTest {

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void everyResultIsDistinguishable() {
        var page = new Page<>(List.of(TestLetters.LETTER), 1, new PageRequest(0, 20));

        assertThat(Stream.of(new ListResult.Found(page), new ListResult.NotAllowed())
                .map(result -> switch (result) {
                    case ListResult.Found found -> 200;
                    case ListResult.NotAllowed notAllowed -> 403;
                })).containsExactly(200, 403);
    }

    @Test
    void foundNeedsAPage() {
        assertThatThrownBy(() -> new ListResult.Found(null)).hasMessageContaining("Page");
    }
}
