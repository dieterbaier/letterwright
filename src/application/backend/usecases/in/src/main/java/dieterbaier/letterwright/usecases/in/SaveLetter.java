package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;

/**
 * Use case "save letter": the snapshot of a preview produces the PDF the user has seen, which is saved only if it is
 * conformant (ADR-005, ADR-007). The same snapshot yields at most one letter; a new one comes only from a new preview
 * (ADR-021).
 */
public interface SaveLetter {

    SaveResult save(UserId user, SnapshotId snapshot);
}
