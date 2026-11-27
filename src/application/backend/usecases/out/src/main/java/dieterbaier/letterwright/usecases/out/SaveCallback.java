package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.letter.CallbackResult;
import dieterbaier.letterwright.domain.model.letter.Letter;

/**
 * Meldet dem Host ein neu angelegtes Schreiben, vor dem Commit der Transaktion (ADR-023). Implementiert von
 * {@code adapter-host}; stellt der Host keinen Rückruf bereit, nimmt die Implementierung jedes Schreiben an.
 */
public interface SaveCallback {

    CallbackResult created(UserId user, Letter letter);
}
