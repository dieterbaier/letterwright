package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.CallbackResult;
import dieterbaier.letterwright.domain.model.letter.Letter;

/**
 * Reports a newly created letter to the host before the transaction commits (ADR-023). Implemented by
 * {@code adapter-host}; if the host provides no callback, the implementation accepts every letter.
 */
public interface SaveCallback {

    CallbackResult created(UserId user, Letter letter);
}
