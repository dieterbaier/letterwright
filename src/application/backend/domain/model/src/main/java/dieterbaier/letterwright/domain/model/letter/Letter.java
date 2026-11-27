package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.validation.Validate;
import java.time.Instant;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * A document created from a letter template and stored. The PDF, its checksum and the template versions used are
 * stored; a letter is never rendered again (ADR-007).
 *
 * <p>Every letter knows the snapshot it was created from. There is at most one letter per snapshot so that saving the
 * same preview again does not create a second one (ADR-021).
 *
 * <p>Whether a letter is still visible is told by its {@link LetterState}. All use cases treat a logically deleted
 * letter like a non-existent one.
 */
@AggregateRoot
public final class Letter {

    @Identity
    private final LetterId id;
    private final SnapshotId snapshot;
    private final BusinessReference reference;
    private final VersionReference letterhead;
    private final VersionReference letterTemplate;
    private final DocumentReference document;
    private final Checksum pdfChecksum;
    private final Instant created;
    private final LetterState state;

    private Letter(LetterId id, SnapshotId snapshot, BusinessReference reference, VersionReference letterhead,
            VersionReference letterTemplate, DocumentReference document, Checksum pdfChecksum, Instant created,
            LetterState state) {
        this.id = Validate.required(id, "The ID of the letter");
        this.snapshot = Validate.required(snapshot, "The snapshot of the letter");
        this.reference = Validate.required(reference, "The business reference of the letter");
        this.letterhead = Validate.required(letterhead, "The letterhead version of the letter");
        this.letterTemplate = Validate.required(letterTemplate, "The letter template version of the letter");
        this.document = Validate.required(document, "The document reference of the letter");
        this.pdfChecksum = Validate.required(pdfChecksum, "The checksum of the PDF");
        this.created = Validate.required(created, "The creation time of the letter");
        this.state = Validate.required(state, "The state of the letter");
    }

    /**
     * Creates the letter from the snapshot its PDF was rendered from, and consumes it. Reference and template versions
     * come from the snapshot so that the letter cannot deviate from the preview. A consumed snapshot yields no further
     * letter (ADR-021).
     *
     * <p>{@code now} is both the time checked against the expiry and the creation time. The use case passes the time
     * of its clock so that both match and tests can check the boundary without waiting.
     *
     * @throws IllegalStateException if the snapshot has expired at {@code now} or is already consumed
     */
    public static LetterFromSnapshot fromSnapshot(
            LetterId id, Snapshot snapshot, DocumentReference document, Checksum pdfChecksum, Instant now) {
        Validate.required(snapshot, "The snapshot");
        if (snapshot.isExpired(Validate.required(now, "The time"))) {
            throw new IllegalStateException("Snapshot " + snapshot.id().value() + " has expired");
        }
        Snapshot consumed = snapshot.consumed();
        return new LetterFromSnapshot(new Letter(id, snapshot.id(), snapshot.reference(), snapshot.letterhead(),
                snapshot.letterTemplate(), document, pdfChecksum, now, new LetterState.Visible()),
                consumed);
    }

    public static Letter restore(LetterId id, SnapshotId snapshot, BusinessReference reference,
            VersionReference letterhead, VersionReference letterTemplate, DocumentReference document,
            Checksum pdfChecksum, Instant created, LetterState state) {
        return new Letter(id, snapshot, reference, letterhead, letterTemplate, document, pdfChecksum, created,
                state);
    }

    /**
     * The letter as it is after a logical deletion: the PDF remains, but it is no longer visible. Whether and how it
     * may be deleted is decided by the host's permissions and deletion policy, not by the letter.
     *
     * @throws IllegalStateException if the letter is already logically deleted
     */
    public Letter logicallyDeleted(Instant now) {
        if (state instanceof LetterState.LogicallyDeleted) {
            throw new IllegalStateException("Letter " + id.value() + " is already logically deleted");
        }
        return new Letter(id, snapshot, reference, letterhead, letterTemplate, document, pdfChecksum, created,
                new LetterState.LogicallyDeleted(Validate.required(now, "The time")));
    }

    public boolean isVisible() {
        return state instanceof LetterState.Visible;
    }

    public LetterId id() {
        return id;
    }

    public SnapshotId snapshot() {
        return snapshot;
    }

    public BusinessReference reference() {
        return reference;
    }

    public VersionReference letterhead() {
        return letterhead;
    }

    public VersionReference letterTemplate() {
        return letterTemplate;
    }

    public DocumentReference document() {
        return document;
    }

    public Checksum pdfChecksum() {
        return pdfChecksum;
    }

    public Instant created() {
        return created;
    }

    public LetterState state() {
        return state;
    }

    /** Two instances with the same ID are equal. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Letter letter && id.equals(letter.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Letter[id=" + id.value() + ", created=" + created + "]";
    }
}
