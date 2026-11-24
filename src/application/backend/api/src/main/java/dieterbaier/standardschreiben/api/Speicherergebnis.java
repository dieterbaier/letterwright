package dieterbaier.standardschreiben.api;

import java.util.List;

/**
 * Was beim Speichern eines Schreibens über die Java-API herauskommt. Nur {@link Gespeichert} und
 * {@link BereitsGespeichert} haben ein Schreiben; alle anderen Fälle sind fachliche Ablehnungen, bei denen nichts
 * gespeichert wurde. Ein Host unterscheidet die Fälle mit {@code switch}:
 *
 * <pre>{@code
 * switch (ergebnis) {
 *     case Speicherergebnis.Gespeichert gespeichert -> verknuepfe(gespeichert.schreiben().id());
 *     case Speicherergebnis.BereitsGespeichert bereits -> verknuepfe(bereits.schreiben().id());
 *     case Speicherergebnis.SnapshotUnbekannt unbekannt -> ...;
 *     case Speicherergebnis.SnapshotAbgelaufen abgelaufen -> ...;
 *     case Speicherergebnis.NichtErlaubt nichtErlaubt -> ...;
 *     case Speicherergebnis.DatenkontextGeaendert geaendert -> ...;
 *     case Speicherergebnis.NichtKonform nichtKonform -> ... nichtKonform.befunde() ...;
 * }
 * }</pre>
 *
 * <p>Eine Ablehnung markiert die Transaktion des Hosts nicht zum Zurückrollen. Ob der Host danach committet oder
 * zurückrollt, entscheidet er selbst.
 */
public sealed interface Speicherergebnis {

    /** Dieser Aufruf hat das Schreiben angelegt. */
    record Gespeichert(GespeichertesSchreiben schreiben) implements Speicherergebnis {

        public Gespeichert {
            Pflicht.wert(schreiben, "Das gespeicherte Schreiben");
        }
    }

    /**
     * Zum Snapshot gab es schon ein Schreiben, etwa nach einem Retry. Dieser Aufruf hat kein neues angelegt (ADR-021).
     * Das vorhandene kann über die Java-API oder über die Oberfläche entstanden sein, auch in derselben, noch nicht
     * committeten Transaktion des Hosts.
     */
    record BereitsGespeichert(GespeichertesSchreiben schreiben) implements Speicherergebnis {

        public BereitsGespeichert {
            Pflicht.wert(schreiben, "Das bereits gespeicherte Schreiben");
        }
    }

    /** Den Snapshot gibt es nicht, er gehört einem anderen Benutzer, oder das Schreiben aus ihm ist gelöscht. */
    record SnapshotUnbekannt() implements Speicherergebnis {}

    /** Der Snapshot ist abgelaufen; eine neue Vorschau ist nötig. */
    record SnapshotAbgelaufen() implements Speicherergebnis {}

    /**
     * Die Berechtigungsprüfung des Hosts erlaubt dem Benutzer nicht mehr, aus dem Standardschreiben zu dem Bezug ein
     * Schreiben zu erzeugen.
     */
    record NichtErlaubt() implements Speicherergebnis {}

    /** Der Host liefert inzwischen andere Daten als in der Vorschau; eine neue Vorschau ist nötig. */
    record DatenkontextGeaendert() implements Speicherergebnis {}

    /**
     * Das erzeugte PDF ist nicht konform (PDF/A, PDF/UA); gespeichert wurde nichts.
     *
     * @param befunde die Befunde der Konformitätsprüfung, in ihrer Reihenfolge; keiner fehlt
     */
    record NichtKonform(List<String> befunde) implements Speicherergebnis {

        public NichtKonform {
            befunde = Pflicht.ohneLuecken(befunde, "Die Befunde");
        }
    }
}
