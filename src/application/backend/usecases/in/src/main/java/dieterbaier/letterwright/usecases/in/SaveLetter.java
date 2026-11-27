package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;

/**
 * Anwendungsfall „Schreiben speichern“: Aus dem Snapshot einer Vorschau entsteht das PDF, das der Benutzer gesehen hat,
 * und wird nur gespeichert, wenn es konform ist (ADR-005, ADR-007). Derselbe Snapshot ergibt höchstens ein Schreiben;
 * ein neues entsteht nur aus einer neuen Vorschau (ADR-021).
 */
public interface SaveLetter {

    SaveResult save(UserId user, SnapshotId snapshot);
}
