package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.validierung.Pruefe;
import java.util.List;

/** Was beim Speichern eines Schreibens herauskommt. Nur die ersten beiden Fälle haben ein Schreiben. */
public sealed interface Speicherergebnis {

    record Gespeichert(SchreibenId schreiben) implements Speicherergebnis {

        public Gespeichert {
            Pruefe.pflicht(schreiben, "Das gespeicherte Schreiben");
        }
    }

    /** Zum Snapshot gab es schon ein Schreiben, etwa nach einem Doppelklick oder einem Retry. Es entstand kein neues. */
    record BereitsGespeichert(SchreibenId schreiben) implements Speicherergebnis {

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

    /** Das erzeugte PDF ist nicht konform; gespeichert wurde nichts. */
    record NichtKonform(List<String> befunde) implements Speicherergebnis {

        public NichtKonform {
            befunde = List.copyOf(Pruefe.pflichtOhneLuecken(befunde, "Die Befunde"));
        }
    }
}
