package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.validation.Validate;
import java.util.List;

/**
 * Was beim Speichern eines Schreibens herauskommt. Nur die ersten beiden Fälle haben ein Schreiben. Sie tragen das ganze
 * Schreiben, weil die Java-API dem Host seine Identifikatoren zurückgibt (ADR-023).
 */
public sealed interface SaveResult {

    record Saved(Letter letter) implements SaveResult {

        public Saved {
            Validate.required(letter, "Das gespeicherte Schreiben");
        }
    }

    /** Zum Snapshot gab es schon ein Schreiben, etwa nach einem Doppelklick oder einem Retry. Es entstand kein neues. */
    record AlreadySaved(Letter letter) implements SaveResult {

        public AlreadySaved {
            Validate.required(letter, "Das bereits gespeicherte Schreiben");
        }
    }

    /** Den Snapshot gibt es nicht, er gehört einem anderen Benutzer, oder das Schreiben aus ihm ist gelöscht. */
    record SnapshotUnknown() implements SaveResult {}

    /** Der Snapshot ist abgelaufen; eine neue Vorschau ist nötig. */
    record SnapshotExpired() implements SaveResult {}

    /** Der Host erlaubt dem Benutzer nicht mehr, aus dem Standardschreiben zu dem Bezug ein Schreiben zu erzeugen. */
    record NotAllowed() implements SaveResult {}

    /** Der Host liefert inzwischen andere Daten als in der Vorschau; eine neue Vorschau ist nötig. */
    record DataContextChanged() implements SaveResult {}

    /**
     * Der Rückruf des Hosts hat das neue Schreiben abgelehnt (ADR-023). Die Bibliothek hat ihre Änderungen schon
     * geschrieben; erst der Rollback an der äußeren Transaktionsgrenze nimmt sie zurück, der Snapshot ist dann wieder
     * offen. Diese Grenze meldet den Grund erst nach dem Rollback.
     */
    record RejectedByHost(String reason) implements SaveResult {

        public RejectedByHost {
            Validate.notBlank(reason, "Der Grund der Ablehnung");
        }
    }

    /** Das erzeugte PDF ist nicht konform; gespeichert wurde nichts. */
    record NotConformant(List<String> findings) implements SaveResult {

        public NotConformant {
            findings = List.copyOf(Validate.requiredWithoutGaps(findings, "Die Befunde"));
        }
    }
}
