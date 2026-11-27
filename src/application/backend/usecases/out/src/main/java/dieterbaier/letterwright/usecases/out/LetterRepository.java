package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.Page;
import dieterbaier.letterwright.domain.model.PageRequest;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterFromSnapshot;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import java.util.Optional;

/**
 * Saves and loads letters. The domain decides that a snapshot yields at most one letter; the repository enforces this
 * atomically against concurrent access (ADR-021).
 */
public interface LetterRepository {

    /** Also a logically deleted letter; the domain decides whether it is visible. */
    Optional<Letter> load(LetterId id);

    /** Also a logically deleted letter; the domain decides whether it is visible. */
    Optional<Letter> forSnapshot(SnapshotId snapshot);

    /**
     * The visible letters for the reference, newest first. The repository filters out logically deleted ones in the
     * query, that is before paginating and counting (QS-009); {@link Letter#isVisible()} defines what visible means.
     */
    Page<Letter> visibleFor(BusinessReference reference, PageRequest request);

    /** Replaces the saved letter with the same ID, for example with the logically deleted one. */
    void replace(Letter changed);

    /** Removes the letter; the caller removes its PDF from the {@link DocumentStore}. */
    void remove(LetterId id);

    /**
     * Saves the letter and replaces the snapshot with the consumed one, both in one step and only if the stored snapshot
     * is still open. If it is already consumed, even by a concurrent request, it saves nothing and returns the existing
     * letter, even a logically deleted one. If that letter has since been hard deleted, it reports so. If the snapshot no
     * longer exists, it saves nothing either.
     *
     * <p>An exception means: whether anything was saved is uncertain. The caller must then not undo anything.
     */
    SaveOutcome save(LetterFromSnapshot creation);
}
