package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.validation.Validate;

/** Was beim Herunterladen eines Schreibens herauskommt. Nur {@link Delivered} enthält ein PDF. */
public sealed interface DownloadResult {

    /**
     * Das PDF mit derselben Prüfsumme wie beim Speichern. Eine Klasse statt eines Records, weil ein Record ein Array per
     * Referenz vergleicht und herausgibt.
     */
    final class Delivered implements DownloadResult {

        private final Letter letter;
        private final byte[] pdf;

        public Delivered(Letter letter, byte[] pdf) {
            this.letter = Validate.required(letter, "Das Schreiben");
            this.pdf = Validate.required(pdf, "Das PDF").clone();
        }

        public Letter letter() {
            return letter;
        }

        public byte[] pdf() {
            return pdf.clone();
        }
    }

    /**
     * Das Schreiben gibt es nicht, es ist logisch gelöscht, oder der Benutzer darf es nicht herunterladen. Die Fälle sind
     * absichtlich nicht unterscheidbar (QS-009).
     */
    record Unknown() implements DownloadResult {}
}
