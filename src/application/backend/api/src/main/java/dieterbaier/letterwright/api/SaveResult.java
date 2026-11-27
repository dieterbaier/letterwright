package dieterbaier.letterwright.api;

import java.util.List;

/**
 * The outcome of saving a letter through the Java API. Only {@link Saved} and {@link AlreadySaved} have a letter; all
 * other cases are business rejections in which nothing was saved. A host distinguishes the cases with {@code switch}:
 *
 * <pre>{@code
 * switch (result) {
 *     case SaveResult.Saved saved -> link(saved.letter().id());
 *     case SaveResult.AlreadySaved already -> link(already.letter().id());
 *     case SaveResult.SnapshotUnknown unknown -> ...;
 *     case SaveResult.SnapshotExpired expired -> ...;
 *     case SaveResult.NotAllowed notAllowed -> ...;
 *     case SaveResult.DataContextChanged changed -> ...;
 *     case SaveResult.NotConformant notConformant -> ... notConformant.findings() ...;
 * }
 * }</pre>
 *
 * <p>A rejection does not mark the host's transaction for rollback. The host decides itself whether to commit or roll
 * back afterwards.
 */
public sealed interface SaveResult {

    /** This call created the letter. */
    record Saved(SavedLetter letter) implements SaveResult {

        public Saved {
            Require.value(letter, "The saved letter");
        }
    }

    /**
     * A letter already existed for the snapshot, for example after a retry. This call did not create a new one
     * (ADR-021). The existing one may have been created through the Java API or through the user interface, even in the
     * same, not yet committed transaction of the host.
     */
    record AlreadySaved(SavedLetter letter) implements SaveResult {

        public AlreadySaved {
            Require.value(letter, "The already saved letter");
        }
    }

    /** The snapshot does not exist, belongs to another user, or the letter created from it has been deleted. */
    record SnapshotUnknown() implements SaveResult {}

    /** The snapshot has expired; a new preview is needed. */
    record SnapshotExpired() implements SaveResult {}

    /**
     * The host's authorization check no longer allows the user to create a letter from the letter template for the
     * reference.
     */
    record NotAllowed() implements SaveResult {}

    /** The host now delivers different data than in the preview; a new preview is needed. */
    record DataContextChanged() implements SaveResult {}

    /**
     * The generated PDF is not conformant (PDF/A, PDF/UA); nothing was saved.
     *
     * @param findings the findings of the conformance check, in their order; none is missing
     */
    record NotConformant(List<String> findings) implements SaveResult {

        public NotConformant {
            findings = Require.withoutGaps(findings, "The findings");
        }
    }
}
