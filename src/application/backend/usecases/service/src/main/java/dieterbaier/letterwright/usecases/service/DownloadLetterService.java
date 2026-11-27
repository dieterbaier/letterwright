package dieterbaier.letterwright.usecases.service;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.usecases.in.DownloadResult;
import dieterbaier.letterwright.usecases.in.DownloadLetter;
import dieterbaier.letterwright.usecases.out.Permissions;
import dieterbaier.letterwright.usecases.out.DocumentStore;
import dieterbaier.letterwright.usecases.out.LetterRepository;

/**
 * A non-existent, a logically deleted and a forbidden letter all yield the same {@code Unknown} (QS-009). It delivers
 * the PDF only with the checksum from saving; if the checksum differs, the stored data is corrupt and it delivers
 * nothing (QS-008).
 */
public final class DownloadLetterService implements DownloadLetter {

    private final LetterRepository letters;
    private final Permissions permissions;
    private final DocumentStore documents;

    public DownloadLetterService(LetterRepository letters, Permissions permissions, DocumentStore documents) {
        this.letters = letters;
        this.permissions = permissions;
        this.documents = documents;
    }

    @Override
    public DownloadResult load(UserId user, LetterId id) {
        return letters.load(id)
                .filter(Letter::isVisible)
                .filter(found -> permissions.may(user, LetterAction.DOWNLOAD, found.reference()))
                .<DownloadResult>map(this::deliver)
                .orElseGet(DownloadResult.Unknown::new);
    }

    private DownloadResult.Delivered deliver(Letter found) {
        byte[] pdf = documents.read(found.document());
        if (!Checksum.of(pdf).equals(found.pdfChecksum())) {
            throw new IllegalStateException(
                    "The PDF of letter " + found.id().value() + " does not match the saved checksum");
        }
        return new DownloadResult.Delivered(found, pdf);
    }
}
