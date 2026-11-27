package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.validation.Validate;

/** Was beim Löschen eines Schreibens herauskommt. */
public sealed interface DeletionResult {

    /** Das Schreiben ist für den Benutzer weg, ob hart oder logisch gelöscht, hat die Löschrichtlinie entschieden. */
    record Deleted() implements DeletionResult {}

    /** Die Löschrichtlinie des Hosts sperrt das Löschen; der Grund ist für den Benutzer bestimmt. */
    record Blocked(String reason) implements DeletionResult {

        public Blocked {
            Validate.notBlank(reason, "Der Grund der Löschsperre");
        }
    }

    /**
     * Das Schreiben gibt es nicht, es ist schon logisch gelöscht, oder der Benutzer darf es nicht löschen. Die Fälle sind
     * absichtlich nicht unterscheidbar (QS-009).
     */
    record Unknown() implements DeletionResult {}
}
