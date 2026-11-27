package dieterbaier.letterwright.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class WithdrawalResultTest {

    private static final VersionReference VERSION = new VersionReference(TemplateId.generate(), VersionNumber.FIRST);

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void everyResultIsDistinguishable() {
        assertThat(Stream.of(new WithdrawalResult.Withdrawn(VERSION), new WithdrawalResult.NotAllowed(),
                new WithdrawalResult.TemplateUnknown(), new WithdrawalResult.NothingPublished(),
                new WithdrawalResult.Stale())
                .map(result -> switch (result) {
                    case WithdrawalResult.Withdrawn withdrawn -> 200;
                    case WithdrawalResult.NotAllowed notAllowed -> 403;
                    case WithdrawalResult.TemplateUnknown unknown -> 404;
                    case WithdrawalResult.NothingPublished nothing -> 422;
                    case WithdrawalResult.Stale stale -> 409;
                })).containsExactly(200, 403, 404, 422, 409);
    }

    @Test
    void withdrawnNamesTheVersion() {
        assertThat(new WithdrawalResult.Withdrawn(VERSION).version()).isEqualTo(VERSION);
        assertThatThrownBy(() -> new WithdrawalResult.Withdrawn(null))
                .hasMessageContaining("zurückgezogene Version");
    }
}
