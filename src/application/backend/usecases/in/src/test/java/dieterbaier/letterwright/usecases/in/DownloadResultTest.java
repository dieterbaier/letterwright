package dieterbaier.letterwright.usecases.in;

import static dieterbaier.letterwright.usecases.in.TestLetters.PDF;
import static dieterbaier.letterwright.usecases.in.TestLetters.LETTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class DownloadResultTest {

    /** This is how an inbound adapter tells the results apart; the compiler checks that none is missing. */
    @Test
    void everyResultIsDistinguishable() {
        assertThat(Stream.of(new DownloadResult.Delivered(LETTER, PDF.clone()), new DownloadResult.Unknown())
                .map(result -> switch (result) {
                    case DownloadResult.Delivered delivered -> 200;
                    case DownloadResult.Unknown unknown -> 404;
                })).containsExactly(200, 404);
    }

    @Test
    void theDeliveredPdfCannotBeChangedFromOutside() {
        byte[] pdf = PDF.clone();
        var delivered = new DownloadResult.Delivered(LETTER, pdf);
        pdf[0] = 9;
        delivered.pdf()[1] = 9;

        assertThat(delivered.pdf()).containsExactly(PDF);
        assertThat(delivered.letter()).isEqualTo(LETTER);
    }

    @Test
    void requiredAttributes() {
        assertThatThrownBy(() -> new DownloadResult.Delivered(null, PDF)).hasMessageContaining("letter");
        assertThatThrownBy(() -> new DownloadResult.Delivered(LETTER, null)).hasMessageContaining("PDF");
    }
}
