package dieterbaier.letterwright.domain.model.snapshot;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * A snapshot is open until a letter is created from it; after that it is consumed (ADR-021). Only an open snapshot
 * holds the data context: once the letter is saved, nobody needs it any more.
 */
@ValueObject
public sealed interface SnapshotState {

    record Open(DataContext dataContext) implements SnapshotState {

        public Open {
            Validate.required(dataContext, "The data context of the snapshot");
        }

        /** Whether the newly read data context has the same checksum as the one of the preview. */
        public boolean isUnchanged(DataContext reread) {
            return dataContext.checksum().equals(reread.checksum());
        }
    }

    /**
     * A letter was created from the snapshot. It remains until its expiry so that a retry can check owner and
     * permission and find the letter.
     */
    record Consumed() implements SnapshotState {}
}
