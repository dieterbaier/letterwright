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

    void loesche(SnapshotId id);
}
