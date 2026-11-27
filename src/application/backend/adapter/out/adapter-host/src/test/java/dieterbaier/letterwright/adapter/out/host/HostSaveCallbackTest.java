package dieterbaier.letterwright.adapter.out.host;

import static dieterbaier.letterwright.adapter.out.host.TestData.ERIKA;
import static dieterbaier.letterwright.adapter.out.host.TestData.ERIKA_IN_SPI;
import static dieterbaier.letterwright.adapter.out.host.TestData.CREATED;
import static dieterbaier.letterwright.adapter.out.host.TestData.CUSTOMER_IN_SPI;
import static dieterbaier.letterwright.adapter.out.host.TestData.REMINDER;
import static dieterbaier.letterwright.adapter.out.host.TestData.LETTER;
import static dieterbaier.letterwright.adapter.out.host.TestData.nullFromHost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.letter.CallbackResult;
import dieterbaier.letterwright.spi.SavedLetter;
import dieterbaier.letterwright.spi.LetterTemplateVersion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HostSaveCallbackTest {

    @Test
    void theHostReceivesTheIdentifiersOfTheNewLetter() {
        List<SavedLetter> received = new ArrayList<>();
        var callback = HostSaveCallback.forReference(letter -> {
            received.add(letter);
            return dieterbaier.letterwright.spi.CallbackResult.accepted();
        });

        assertThat(callback.created(ERIKA, LETTER)).isEqualTo(CallbackResult.accepted());
        assertThat(received).containsExactly(new SavedLetter(LETTER.id().value(), CUSTOMER_IN_SPI,
                new LetterTemplateVersion(REMINDER.id().value(), 1), ERIKA_IN_SPI, CREATED));
    }

    @Test
    void theHostsRejectionComesWithItsReason() {
        var callback = HostSaveCallback.forReference(
                letter -> dieterbaier.letterwright.spi.CallbackResult.rejected("Aufgabe ist erledigt"));

        assertThat(callback.created(ERIKA, LETTER)).isEqualTo(CallbackResult.rejected("Aufgabe ist erledigt"));
    }

    @Test
    void nullFromTheCallbackIsAContractViolation() {
        var callback = HostSaveCallback.forReference(letter -> nullFromHost());

        assertThatThrownBy(() -> callback.created(ERIKA, LETTER))
                .isInstanceOf(HostContractViolated.class)
                .hasMessageContaining("null");
    }

    @Test
    void anExceptionOfTheHostStaysUnchanged() {
        var error = new IllegalStateException("Aufgabenverwaltung nicht erreichbar");
        var callback = HostSaveCallback.forReference(letter -> {
            throw error;
        });

        assertThatThrownBy(() -> callback.created(ERIKA, LETTER)).isSameAs(error);
    }

    @Test
    void withoutCallbackEveryLetterIsAccepted() {
        assertThat(HostSaveCallback.none().created(ERIKA, LETTER)).isEqualTo(CallbackResult.accepted());
    }
}
