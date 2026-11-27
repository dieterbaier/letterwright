package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.LetterId;

/**
 * Use case "delete letter": the host's deletion policy decides whether and how a letter is deleted (RB-10, ADR-007). A
 * letter the user may not delete does not exist for them (QS-009).
 */
public interface DeleteLetter {

    DeletionResult delete(UserId user, LetterId letter);
}
