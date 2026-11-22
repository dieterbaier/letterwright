package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import java.util.Optional;

/**
 * Hält Snapshots bis zu ihrem Ablauf. Ob ein Snapshot dem Anfragenden gehört, prüft der Anwendungsfall, nicht die
 * Ablage.
 */
public interface Snapshotablage {

    void speichere(Snapshot snapshot);

    Optional<Snapshot> lade(SnapshotId id);

    /**
     * Löscht den Snapshot, aber nur, wenn er beim Löschen noch offen ist; beides in einem Schritt. Einen verbrauchten
     * lässt sie stehen, auch wenn eine gleichzeitige Anfrage ihn gerade erst verbraucht hat: Eine Wiederholung des
     * Speicherns braucht ihn noch (ADR-021).
     */
    void loescheWennOffen(SnapshotId id);
}
