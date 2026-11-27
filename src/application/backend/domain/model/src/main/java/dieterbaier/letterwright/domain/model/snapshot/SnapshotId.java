package dieterbaier.letterwright.domain.model.snapshot;

import dieterbaier.letterwright.validation.Validate;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Identity of a snapshot.
 *
 * <p>The constructor restores an existing identity, {@link #generate()} assigns a new one.
 */
@ValueObject
public record SnapshotId(UUID value) {

    public SnapshotId {
        Validate.required(value, "The value of the SnapshotId");
    }

    public static SnapshotId generate() {
        return new SnapshotId(UUID.randomUUID());
    }
}
