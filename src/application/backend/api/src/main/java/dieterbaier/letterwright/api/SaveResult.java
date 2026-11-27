package dieterbaier.letterwright.api;

import java.util.List;

/**
 * Was beim Speichern eines Schreibens über die Java-API herauskommt. Nur {@link Saved} und
 * {@link AlreadySaved} haben ein Schreiben; alle anderen Fälle sind fachliche Ablehnungen, bei denen nichts
 * gespeichert wurde. Ein Host unterscheidet die Fälle mit {@code switch}:
 *
 * <pre>{@code
 * switch (result) {
 *     case SaveResult.Saved saved -> verknuepfe(saved.letter().id());
 *     case SaveResult.AlreadySaved already -> verknuepfe(already.letter().id());
 *     case SaveResult.SnapshotUnknown unknown -> ...;
 *     case SaveResult.SnapshotExpired expired -> ...;
 *     case SaveResult.NotAllowed notAllowed -> ...;
 *     case SaveResult.DataContextChanged changed -> ...;
 *     case SaveResult.NotConformant notConformant -> ... notConformant.findings() ...;
 * }
 * }</pre>
 *
 * <p>Eine Ablehnung markiert die Transaktion des Hosts nicht zum Zurückrollen. Ob der Host danach committet oder
 * zurückrollt, entscheidet er selbst.
 */
public sealed interface SaveResult {

    /** Dieser Aufruf hat das Schreiben angelegt. */
    record Saved(SavedLetter letter) implements SaveResult {

        public Saved {
            Require.value(letter, "Das gespeicherte Schreiben");
        }
    }

    /**
     * Zum Snapshot gab es schon ein Schreiben, etwa nach einem Retry. Dieser Aufruf hat kein neues angelegt (ADR-021).
     * Das vorhandene kann über die Java-API oder über die Oberfläche entstanden sein, auch in derselben, noch nicht
     * committeten Transaktion des Hosts.
     */
    record AlreadySaved(SavedLetter letter) implements SaveResult {

        public AlreadySaved {
            Require.value(letter, "Das bereits gespeicherte Schreiben");
        }
    }

    /** Den Snapshot gibt es nicht, er gehört einem anderen Benutzer, oder das Schreiben aus ihm ist gelöscht. */
    record SnapshotUnknown() implements SaveResult {}

    /** Der Snapshot ist abgelaufen; eine neue Vorschau ist nötig. */
    record SnapshotExpired() implements SaveResult {}

    /**
     * Die Berechtigungsprüfung des Hosts erlaubt dem Benutzer nicht mehr, aus dem Standardschreiben zu dem Bezug ein
     * Schreiben zu erzeugen.
     */
    record NotAllowed() implements SaveResult {}

    /** Der Host liefert inzwischen andere Daten als in der Vorschau; eine neue Vorschau ist nötig. */
    record DataContextChanged() implements SaveResult {}

    /**
     * Das erzeugte PDF ist nicht konform (PDF/A, PDF/UA); gespeichert wurde nichts.
     *
     * @param findings die Befunde der Konformitätsprüfung, in ihrer Reihenfolge; keiner fehlt
     */
    record NotConformant(List<String> findings) implements SaveResult {

        public NotConformant {
            findings = Require.withoutGaps(findings, "Die Befunde");
        }
    }
}
