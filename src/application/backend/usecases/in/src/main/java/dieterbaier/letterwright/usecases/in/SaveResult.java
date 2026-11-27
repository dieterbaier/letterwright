package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.validation.Validate;
import java.util.List;

/**
 * The outcome of saving a letter. Only the first two cases have a letter. They carry the whole letter because the Java
 * API returns its identifiers to the host (ADR-023).
 */
public sealed interface SaveResult {

    record Saved(Letter letter) implements SaveResult {

        public Saved {
            Validate.required(letter, "The saved letter");
        }
    }

    /** The snapshot already had a letter, for example after a double click or a retry. No new one was created. */
    record AlreadySaved(Letter letter) implements SaveResult {

        public AlreadySaved {
            Validate.required(letter, "The already saved letter");
        }
    }

    /** The snapshot does not exist, belongs to another user, or the letter created from it is deleted. */
    record SnapshotUnknown() implements SaveResult {}

    /** The snapshot has expired; a new preview is needed. */
    record SnapshotExpired() implements SaveResult {}

    /** The host no longer allows the user to create a letter from the letter template for the reference. */
    record NotAllowed() implements SaveResult {}

    /** The host now provides different data than in the preview; a new preview is needed. */
    record DataContextChanged() implements SaveResult {}

    /**
     * The host's callback rejected the new letter (ADR-023). The library has already written its changes; only the
     * rollback at the outer transaction boundary undoes them, and the snapshot is then open again. That boundary reports
     * the reason only after the rollback.
     */
    record RejectedByHost(String reason) implements SaveResult {

        public RejectedByHost {
            Validate.notBlank(reason, "The reason for the rejection");
        }
    }

    /** The generated PDF is not conformant; nothing was saved. */
    record NotConformant(List<String> findings) implements SaveResult {

        public NotConformant {
            findings = List.copyOf(Validate.requiredWithoutGaps(findings, "The findings"));
        }
    }
}
