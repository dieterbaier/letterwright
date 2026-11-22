package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenAusSnapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import java.util.Optional;

/**
 * Speichert und lädt Schreiben. Dass aus einem Snapshot höchstens ein Schreiben entsteht, entscheidet die Domäne; das
 * Repository setzt es gegen gleichzeitige Zugriffe atomar durch (ADR-021).
 */
public interface SchreibenRepository {

    Optional<Schreiben> zuSnapshot(SnapshotId snapshot);

    /**
     * Speichert das Schreiben und ersetzt den Snapshot durch den verbrauchten, beides in einem Schritt und nur, wenn der
     * gespeicherte Snapshot noch offen ist. Ist er schon verbraucht, auch durch eine gleichzeitige Anfrage, speichert
     * es nichts und liefert das vorhandene Schreiben.
     *
     * <p>Eine Ausnahme heißt: Ob gespeichert wurde, ist ungewiss. Der Aufrufer darf dann nichts zurücknehmen.
     */
    Speicherausgang speichere(SchreibenAusSnapshot neu);
}
