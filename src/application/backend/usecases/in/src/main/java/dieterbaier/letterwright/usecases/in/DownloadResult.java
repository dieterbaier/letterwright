package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.validation.Validate;

/** The outcome of downloading a letter. Only {@link Delivered} contains a PDF. */
public sealed interface DownloadResult {

    /**
     * The PDF with the same checksum as when it was saved. A class instead of a record, because a record compares and
     * hands out an array by reference.
     */
    final class Delivered implements DownloadResult {

        private final Letter letter;
        private final byte[] pdf;

        public Delivered(Letter letter, byte[] pdf) {
            this.letter = Validate.required(letter, "The letter");
            this.pdf = Validate.required(pdf, "The PDF").clone();
        }

        public Letter letter() {
            return letter;
        }

        public byte[] pdf() {
            return pdf.clone();
        }
    }

    /**
     * The letter does not exist, is logically deleted, or the user may not download it. The cases are deliberately
     * indistinguishable (QS-009).
     */
    record Unknown() implements DownloadResult {}
}
