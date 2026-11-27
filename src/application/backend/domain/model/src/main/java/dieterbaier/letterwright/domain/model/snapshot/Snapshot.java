package dieterbaier.letterwright.domain.model.snapshot;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.validation.Validate;
import java.time.Duration;
import java.time.Instant;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Temporarily stored data context of a preview, from which the PDF is created on saving (ADR-007).
 *
 * <p>A snapshot is bound to the user, the business reference and both template versions. For another user or
 * reference and after expiry it is not usable; the use case then treats it like a non-existent one. It knows the
 * template versions only as {@link VersionReference}; they belong to their templates.
 *
 * <p>A snapshot yields at most one letter: {@link #consumed()} starts only from an open one and releases the data
 * context (ADR-021).
 *
 * <p>The methods receive the time as a parameter. The use case reads its clock once and checks and creates with the
 * same time; the model itself reads no clock.
 */
@AggregateRoot
public final class Snapshot {

    /** How long a snapshot is usable after its creation (ADR-007). */
    public static final Duration VALIDITY = Duration.ofMinutes(30);

    @Identity
    private final SnapshotId id;
    private final UserId user;
    private final BusinessReference reference;
    private final VersionReference letterhead;
    private final VersionReference letterTemplate;
    private final SnapshotState state;
    private final Instant expiry;

    private Snapshot(SnapshotId id, UserId user, BusinessReference reference, VersionReference letterhead,
            VersionReference letterTemplate, SnapshotState state, Instant expiry) {
        this.id = Validate.required(id, "The ID of the snapshot");
        this.user = Validate.required(user, "The user of the snapshot");
        this.reference = Validate.required(reference, "The business reference of the snapshot");
        this.letterhead = Validate.required(letterhead, "The letterhead version of the snapshot");
        this.letterTemplate = Validate.required(letterTemplate, "The letter template version of the snapshot");
        this.state = Validate.required(state, "The state of the snapshot");
        this.expiry = Validate.required(expiry, "The expiry time of the snapshot");
    }

    /** Creates a snapshot that expires {@link #VALIDITY} after {@code now}. */
    public static Snapshot create(SnapshotId id, UserId user, BusinessReference reference,
            VersionReference letterhead, VersionReference letterTemplate, DataContext dataContext, Instant now) {
        return new Snapshot(id, user, reference, letterhead, letterTemplate,
                new SnapshotState.Open(dataContext),
                Validate.required(now, "The time of creation").plus(VALIDITY));
    }

    /** Restores a stored snapshot with its state and expiry time. */
    public static Snapshot restore(SnapshotId id, UserId user, BusinessReference reference,
            VersionReference letterhead, VersionReference letterTemplate, SnapshotState state, Instant expiry) {
        return new Snapshot(id, user, reference, letterhead, letterTemplate, state, expiry);
    }

    /**
     * The snapshot after a letter was created from it, without data context. Meant for {@code Letter.fromSnapshot}.
     *
     * @throws IllegalStateException if a letter was already created from the snapshot
     */
    public Snapshot consumed() {
        if (state instanceof SnapshotState.Consumed) {
            throw new IllegalStateException("A letter was already created from snapshot " + id.value());
        }
        return new Snapshot(id, user, reference, letterhead, letterTemplate, new SnapshotState.Consumed(),
                expiry);
    }

    public SnapshotId id() {
        return id;
    }

    public UserId user() {
        return user;
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

    public SnapshotState state() {
        return state;
    }

    public Instant expiry() {
        return expiry;
    }

    /** From the expiry time on, the snapshot is expired. */
    public boolean isExpired(Instant now) {
        return !now.isBefore(expiry);
    }

    /** Usable only for the same user and reference and only before expiry. */
    public boolean isUsableFor(UserId requester, BusinessReference requestedReference, Instant now) {
        return user.equals(requester)
                && reference.equals(requestedReference)
                && !isExpired(now);
    }

    /** Two instances with the same ID are equal. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Snapshot snapshot && id.equals(snapshot.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        // Without data context: it may contain personal data.
        return "Snapshot[id=" + id.value() + ", expiry=" + expiry + "]";
    }
}
