package dieterbaier.letterwright.domain.model.snapshot;

import dieterbaier.letterwright.validation.Validate;
import java.util.UUID;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Identität eines Snapshots.
 *
 * <p>Der Konstruktor stellt eine bestehende Identität wieder her, {@link #generate()} vergibt eine neue.
 */
@ValueObject
public record SnapshotId(UUID value) {

    public SnapshotId {
        Validate.required(value, "Der Wert der SnapshotId");
    }

    public static SnapshotId generate() {
        return new SnapshotId(UUID.randomUUID());
    }
}
