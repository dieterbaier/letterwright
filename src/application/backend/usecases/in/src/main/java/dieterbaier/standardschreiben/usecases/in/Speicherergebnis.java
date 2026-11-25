package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.validierung.Pruefe;
import java.util.List;

/**
 * Was beim Speichern eines Schreibens herauskommt. Nur die ersten beiden Fälle haben ein Schreiben. Sie tragen das ganze
 * Schreiben, weil die Java-API dem Host seine Identifikatoren zurückgibt (ADR-023).
 */
public sealed interface Speicherergebnis {

    record Gespeichert(Schreiben schreiben) implements Speicherergebnis {

        public Gespeichert {
            Pruefe.pflicht(schreiben, "Das gespeicherte Schreiben");
        }
    }

    /** Zum Snapshot gab es schon ein Schreiben, etwa nach einem Doppelklick oder einem Retry. Es entstand kein neues. */
    record BereitsGespeichert(Schreiben schreiben) implements Speicherergebnis {

        public BereitsGespeichert {
            Pruefe.pflicht(schreiben, "Das bereits gespeicherte Schreiben");
        }
    }

    /** Den Snapshot gibt es nicht, er gehört einem anderen Benutzer, oder das Schreiben aus ihm ist gelöscht. */
    record SnapshotUnbekannt() implements Speicherergebnis {}

    /** Der Snapshot ist abgelaufen; eine neue Vorschau ist nötig. */
    record SnapshotAbgelaufen() implements Speicherergebnis {}

    /** Der Host erlaubt dem Benutzer nicht mehr, aus dem Standardschreiben zu dem Bezug ein Schreiben zu erzeugen. */
    record NichtErlaubt() implements Speicherergebnis {}

    /** Der Host liefert inzwischen andere Daten als in der Vorschau; eine neue Vorschau ist nötig. */
    record DatenkontextGeaendert() implements Speicherergebnis {}

    /**
     * Der Rückruf des Hosts hat das neue Schreiben abgelehnt (ADR-023). Die Bibliothek hat ihre Änderungen schon
     * geschrieben; erst der Rollback an der äußeren Transaktionsgrenze nimmt sie zurück, der Snapshot ist dann wieder
     * offen. Diese Grenze meldet den Grund erst nach dem Rollback.
     */
    record VomHostAbgelehnt(String grund) implements Speicherergebnis {

        public VomHostAbgelehnt {
            Pruefe.nichtBlank(grund, "Der Grund der Ablehnung");
        }
    }

    /** Das erzeugte PDF ist nicht konform; gespeichert wurde nichts. */
    record NichtKonform(List<String> befunde) implements Speicherergebnis {

        public NichtKonform {
            befunde = List.copyOf(Pruefe.pflichtOhneLuecken(befunde, "Die Befunde"));
        }
    }
}
