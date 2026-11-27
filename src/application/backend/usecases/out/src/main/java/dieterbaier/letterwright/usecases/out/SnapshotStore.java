package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import java.util.Optional;

/**
 * Hält Snapshots bis zu ihrem Ablauf. Ob ein Snapshot dem Anfragenden gehört, prüft der Anwendungsfall, nicht die
 * Ablage.
 */
public interface SnapshotStore {

    void save(Snapshot snapshot);

    Optional<Snapshot> load(SnapshotId id);

    /**
     * Löscht den Snapshot, aber nur, wenn er beim Löschen noch offen ist; beides in einem Schritt. Einen verbrauchten
     * lässt sie stehen, auch wenn eine gleichzeitige Anfrage ihn gerade erst verbraucht hat: Eine Wiederholung des
     * Speicherns braucht ihn noch (ADR-021).
     */
    void deleteIfOpen(SnapshotId id);
}
