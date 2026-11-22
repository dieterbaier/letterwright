package dieterbaier.standardschreiben.domaene.modell.snapshot;

import dieterbaier.standardschreiben.validierung.Pruefe;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Identität eines Snapshots.
 *
 * <p>Der Konstruktor stellt eine bestehende Identität wieder her, {@link #neu()} vergibt eine neue.
 */
@ValueObject
public record SnapshotId(UUID wert) {

    public SnapshotId {
        Pruefe.pflicht(wert, "Der Wert der SnapshotId");
    }

    public static SnapshotId neu() {
        return new SnapshotId(UUID.randomUUID());
    }
}
