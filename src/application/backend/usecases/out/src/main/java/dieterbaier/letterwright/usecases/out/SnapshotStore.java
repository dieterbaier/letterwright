package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import java.util.Optional;

/**
 * Keeps snapshots until they expire. The use case, not the store, checks whether a snapshot belongs to the requester.
 */
public interface SnapshotStore {

    void save(Snapshot snapshot);

    Optional<Snapshot> load(SnapshotId id);

    /**
     * Deletes the snapshot, but only if it is still open at the time of deletion; both in one step. It leaves a consumed
     * one in place, even if a concurrent request has only just consumed it: a retry of the save still needs it
     * (ADR-021).
     */
    void deleteIfOpen(SnapshotId id);
}
