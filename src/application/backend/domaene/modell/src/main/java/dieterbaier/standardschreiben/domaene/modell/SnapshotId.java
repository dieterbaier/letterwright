package dieterbaier.standardschreiben.domaene.modell;

import java.util.UUID;

/** Identität eines Snapshots. */
public record SnapshotId(UUID wert) {

    /** Erzeugt eine neue, zufällige Identität. */
    public static SnapshotId neu() {
        return new SnapshotId(UUID.randomUUID());
    }
}
