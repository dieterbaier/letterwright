package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.DeletionDecision;
import dieterbaier.letterwright.domain.model.letter.Letter;

/** Asks the host's deletion policy. Implemented by {@code adapter-host} (ADR-016). */
public interface DeletionRules {

    DeletionDecision decide(UserId user, Letter letter);
}
