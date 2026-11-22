package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.validierung.Pruefe;

/** Was beim Löschen eines Schreibens herauskommt. */
public sealed interface Loeschergebnis {

    /** Das Schreiben ist für den Benutzer weg, ob hart oder logisch gelöscht, hat die Löschrichtlinie entschieden. */
    record Geloescht() implements Loeschergebnis {}

    /** Die Löschrichtlinie des Hosts sperrt das Löschen; der Grund ist für den Benutzer bestimmt. */
    record Gesperrt(String grund) implements Loeschergebnis {

        public Gesperrt {
            Pruefe.nichtBlank(grund, "Der Grund der Löschsperre");
        }
    }

    /**
     * Das Schreiben gibt es nicht, es ist schon logisch gelöscht, oder der Benutzer darf es nicht löschen. Die Fälle sind
     * absichtlich nicht unterscheidbar (QS-009).
     */
    record Unbekannt() implements Loeschergebnis {}
}
