package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.LetterId;

/**
 * Use case "download letter": the unchanged PDF of a saved letter (ADR-007, QS-008). A letter the user may not download
 * does not exist for them (QS-009).
 */
public interface DownloadLetter {

    DownloadResult load(UserId user, LetterId letter);
}
