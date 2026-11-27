package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.LetterId;

/**
 * Anwendungsfall „Schreiben herunterladen“: das unveränderte PDF eines gespeicherten Schreibens (ADR-007, QS-008). Ein
 * Schreiben, das der Benutzer nicht herunterladen darf, gibt es für ihn nicht (QS-009).
 */
public interface DownloadLetter {

    DownloadResult load(UserId user, LetterId letter);
}
