package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.Page;
import dieterbaier.letterwright.domain.model.PageRequest;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterFromSnapshot;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import java.util.Optional;

/**
 * Speichert und lädt Schreiben. Dass aus einem Snapshot höchstens ein Schreiben entsteht, entscheidet die Domäne; das
 * Repository setzt es gegen gleichzeitige Zugriffe atomar durch (ADR-021).
 */
public interface LetterRepository {

    /** Auch ein logisch gelöschtes Schreiben; ob es sichtbar ist, entscheidet die Domäne. */
    Optional<Letter> load(LetterId id);

    /** Auch ein logisch gelöschtes Schreiben; ob es sichtbar ist, entscheidet die Domäne. */
    Optional<Letter> forSnapshot(SnapshotId snapshot);

    /**
     * Die sichtbaren Schreiben zum Bezug, neueste zuerst. Logisch gelöschte filtert das Repository in der Abfrage, also
     * vor dem Paginieren und Zählen (QS-009); was sichtbar heißt, legt {@link Letter#isVisible()} fest.
     */
    Page<Letter> visibleFor(BusinessReference reference, PageRequest request);

    /** Ersetzt das gespeicherte Schreiben mit derselben ID, etwa durch das logisch gelöschte. */
    void replace(Letter changed);

    /** Entfernt das Schreiben; sein PDF entfernt der Aufrufer aus der {@link DocumentStore}. */
    void remove(LetterId id);

    /**
     * Speichert das Schreiben und ersetzt den Snapshot durch den verbrauchten, beides in einem Schritt und nur, wenn der
     * gespeicherte Snapshot noch offen ist. Ist er schon verbraucht, auch durch eine gleichzeitige Anfrage, speichert
     * es nichts und liefert das vorhandene Schreiben, auch ein logisch gelöschtes. Ist dieses inzwischen hart gelöscht,
     * meldet es das. Gibt es den Snapshot nicht mehr, speichert es ebenfalls nichts.
     *
     * <p>Eine Ausnahme heißt: Ob gespeichert wurde, ist ungewiss. Der Aufrufer darf dann nichts zurücknehmen.
     */
    SaveOutcome save(LetterFromSnapshot generate);
}
