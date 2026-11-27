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
 * Ein nicht vorhandenes, ein logisch gelöschtes und ein nicht erlaubtes Schreiben ergeben dasselbe {@code Unknown}
 * (QS-009). Das PDF liefert es nur mit der Prüfsumme vom Speichern aus; weicht sie ab, ist der Bestand defekt, und es
 * liefert nichts (QS-008).
 */
public final class DownloadLetterService implements DownloadLetter {

    private final LetterRepository letter;
    private final Permissions permissions;
    private final DocumentStore documents;

    public DownloadLetterService(LetterRepository letter, Permissions permissions, DocumentStore documents) {
        this.letter = letter;
        this.permissions = permissions;
        this.documents = documents;
    }

    @Override
    public DownloadResult load(UserId user, LetterId id) {
        return letter.load(id)
                .filter(Letter::isVisible)
                .filter(found -> permissions.may(user, LetterAction.DOWNLOAD, found.reference()))
                .<DownloadResult>map(this::deliver)
                .orElseGet(DownloadResult.Unknown::new);
    }

    private DownloadResult.Delivered deliver(Letter found) {
        byte[] pdf = documents.read(found.document());
        if (!Checksum.of(pdf).equals(found.pdfChecksum())) {
            throw new IllegalStateException(
                    "Das PDF von Schreiben " + found.id().value() + " weicht von der gespeicherten Prüfsumme ab");
        }
        return new DownloadResult.Delivered(found, pdf);
    }
}
