package dieterbaier.letterwright.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.usecases.in.DownloadResult;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Bridge to {@code features/letters/download-letter.feature}: one test per scenario, named after the
 * scenario title. The outbound ports are {@link Fakes}.
 */
class DownloadLetterServiceTest {

    private static final Instant SAVED = Instant.parse("2026-11-20T09:00:00Z");
    private static final UserId ERIKA = new UserId("erika");
    private static final UserId MAX = new UserId("max");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Customer", "4711");

    private final Fakes.Host host = new Fakes.Host();
    private final Fakes.Documents documents = new Fakes.Documents();
    private final Fakes.LetterStore letters = new Fakes.LetterStore(new Fakes.Snapshots());
    private final SavedLetters saved = new SavedLetters(letters, documents);
    private final DownloadLetterService retrieval = new DownloadLetterService(letters, host, documents);

    // Background: a letter for customer 4711; Erika may download it.
    private final Letter reminder = saved.save(CUSTOMER_4711, SAVED);

    {
        host.allow(ERIKA, LetterAction.DOWNLOAD, CUSTOMER_4711);
    }

    @Test
    void theDownloadDeliversTheUnchangedPdf() {
        var result = (DownloadResult.Delivered) retrieval.load(ERIKA, reminder.id());

        assertThat(Checksum.of(result.pdf())).isEqualTo(reminder.pdfChecksum());
        assertThat(result.pdf()).isEqualTo(documents.stored.get(reminder.document()));
        assertThat(result.letter()).isEqualTo(reminder);
        assertThat(host.letterActionRequested)
                .containsExactly(new Fakes.Grant(ERIKA, LetterAction.DOWNLOAD, CUSTOMER_4711));
    }

    @Test
    void aLetterThatIsNotAllowedBehavesLikeAMissingOne() {
        var result = retrieval.load(MAX, reminder.id());

        assertThat(result).isEqualTo(new DownloadResult.Unknown());
        assertThat(documents.read).as("no PDF read").isZero();
    }

    @Test
    void anUnknownLetterDoesNotExist() {
        var result = retrieval.load(ERIKA, LetterId.generate());

        assertThat(result).isEqualTo(new DownloadResult.Unknown());
        assertThat(host.wasAsked()).isFalse();
    }

    @Test
    void aLogicallyDeletedLetterDoesNotExist() {
        saved.deleteLogically(reminder, SAVED.plusSeconds(60));

        var result = retrieval.load(ERIKA, reminder.id());

        assertThat(result).isEqualTo(new DownloadResult.Unknown());
        assertThat(host.wasAsked()).isFalse();
        assertThat(documents.read).isZero();
    }

    @Test
    void aChangedPdfIsNotDelivered() {
        documents.stored.put(reminder.document(), "%PDF changed".getBytes(StandardCharsets.UTF_8));

        assertThatIllegalStateException().isThrownBy(() -> retrieval.load(ERIKA, reminder.id()))
                .withMessageContaining("checksum")
                .withMessageNotContaining("4711");
    }
}
