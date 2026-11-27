package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.DeletionDecision;
import dieterbaier.letterwright.domain.model.letter.Letter;

/** Fragt die Löschrichtlinie des Hosts. Implementiert von {@code adapter-host} (ADR-016). */
public interface DeletionRules {

    DeletionDecision decide(UserId user, Letter letter);
}
