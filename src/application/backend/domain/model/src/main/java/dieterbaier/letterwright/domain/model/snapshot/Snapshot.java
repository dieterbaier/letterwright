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
 * Befristet gespeicherter Datenkontext einer Vorschau, aus dem beim Speichern das PDF erzeugt wird (ADR-007).
 *
 * <p>Ein Snapshot ist an Benutzer, fachlichen Bezug und beide Vorlagenversionen gebunden. Für einen anderen Benutzer
 * oder Bezug und nach Ablauf ist er nicht verwendbar; der Anwendungsfall behandelt ihn dann wie einen nicht
 * vorhandenen. Die Vorlagenversionen kennt er nur als {@link VersionReference}, sie gehören zu ihren Vorlagen.
 *
 * <p>Aus einem Snapshot entsteht höchstens ein Schreiben: {@link #consumed()} geht nur von einem offenen aus und
 * gibt den Datenkontext frei (ADR-021).
 *
 * <p>Den Zeitpunkt bekommen die Methoden als Parameter. Der Anwendungsfall liest seine Uhr einmal und prüft und erzeugt
 * mit demselben Zeitpunkt; das Modell selbst liest keine Uhr.
 */
@AggregateRoot
public final class Snapshot {

    /** So lange ist ein Snapshot nach seiner Anlage verwendbar (ADR-007). */
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
        this.id = Validate.required(id, "Die ID des Snapshots");
        this.user = Validate.required(user, "Der Benutzer des Snapshots");
        this.reference = Validate.required(reference, "Der fachliche Bezug des Snapshots");
        this.letterhead = Validate.required(letterhead, "Die Briefpapier-Version des Snapshots");
        this.letterTemplate = Validate.required(letterTemplate, "Die Standardschreiben-Version des Snapshots");
        this.state = Validate.required(state, "Der Zustand des Snapshots");
        this.expiry = Validate.required(expiry, "Der Ablaufzeitpunkt des Snapshots");
    }

    /** Legt einen Snapshot an, der {@link #VALIDITY} nach {@code now} abläuft. */
    public static Snapshot create(SnapshotId id, UserId user, BusinessReference reference,
            VersionReference letterhead, VersionReference letterTemplate, DataContext dataContext, Instant now) {
        return new Snapshot(id, user, reference, letterhead, letterTemplate,
                new SnapshotState.Open(dataContext),
                Validate.required(now, "Der Zeitpunkt der Anlage").plus(VALIDITY));
    }

    /** Stellt einen gespeicherten Snapshot mit Zustand und Ablaufzeitpunkt wieder her. */
    public static Snapshot restore(SnapshotId id, UserId user, BusinessReference reference,
            VersionReference letterhead, VersionReference letterTemplate, SnapshotState state, Instant expiry) {
        return new Snapshot(id, user, reference, letterhead, letterTemplate, state, expiry);
    }

    /**
     * Der Snapshot, nachdem aus ihm ein Schreiben entstanden ist, ohne Datenkontext. Gedacht für
     * {@code Letter.fromSnapshot}.
     *
     * @throws IllegalStateException wenn aus dem Snapshot schon ein Schreiben entstanden ist
     */
    public Snapshot consumed() {
        if (state instanceof SnapshotState.Consumed) {
            throw new IllegalStateException("Aus Snapshot " + id.value() + " ist schon ein Schreiben entstanden");
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

    /** Ab dem Ablaufzeitpunkt ist der Snapshot abgelaufen. */
    public boolean isExpired(Instant now) {
        return !now.isBefore(expiry);
    }

    /** Verwendbar nur für denselben Benutzer und Bezug und nur vor dem Ablauf. */
    public boolean isUsableFor(UserId requester, BusinessReference requestedReference, Instant now) {
        return user.equals(requester)
                && reference.equals(requestedReference)
                && !isExpired(now);
    }

    /** Gleich sind zwei Instanzen mit derselben ID. */
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
        // Ohne Datenkontext: Er kann personenbezogen sein.
        return "Snapshot[id=" + id.value() + ", ablauf=" + expiry + "]";
    }
}
