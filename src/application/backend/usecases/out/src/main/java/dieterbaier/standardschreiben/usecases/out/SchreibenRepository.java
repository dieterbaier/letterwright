package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Seite;
import dieterbaier.standardschreiben.domaene.modell.Seitenanfrage;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenAusSnapshot;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import java.util.Optional;

/**
 * Speichert und lädt Schreiben. Dass aus einem Snapshot höchstens ein Schreiben entsteht, entscheidet die Domäne; das
 * Repository setzt es gegen gleichzeitige Zugriffe atomar durch (ADR-021).
 */
public interface SchreibenRepository {

    /** Auch ein logisch gelöschtes Schreiben; ob es sichtbar ist, entscheidet die Domäne. */
    Optional<Schreiben> lade(SchreibenId id);

    /** Auch ein logisch gelöschtes Schreiben; ob es sichtbar ist, entscheidet die Domäne. */
    Optional<Schreiben> zuSnapshot(SnapshotId snapshot);

    /**
     * Die sichtbaren Schreiben zum Bezug, neueste zuerst. Logisch gelöschte filtert das Repository in der Abfrage, also
     * vor dem Paginieren und Zählen (QS-009); was sichtbar heißt, legt {@link Schreiben#istSichtbar()} fest.
     */
    Seite<Schreiben> sichtbareZu(FachlicherBezug bezug, Seitenanfrage anfrage);

    /** Ersetzt das gespeicherte Schreiben mit derselben ID, etwa durch das logisch gelöschte. */
    void ersetze(Schreiben geaendert);

    /** Entfernt das Schreiben; sein PDF entfernt der Aufrufer aus der {@link DokumentAblage}. */
    void entferne(SchreibenId id);

    /**
     * Speichert das Schreiben und ersetzt den Snapshot durch den verbrauchten, beides in einem Schritt und nur, wenn der
     * gespeicherte Snapshot noch offen ist. Ist er schon verbraucht, auch durch eine gleichzeitige Anfrage, speichert
     * es nichts und liefert das vorhandene Schreiben. Gibt es ihn nicht mehr, speichert es ebenfalls nichts.
     *
     * <p>Eine Ausnahme heißt: Ob gespeichert wurde, ist ungewiss. Der Aufrufer darf dann nichts zurücknehmen.
     */
    Speicherausgang speichere(SchreibenAusSnapshot neu);
}
