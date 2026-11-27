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
 * Ein aus einem Standardschreiben erzeugtes und gespeichertes Dokument. Gespeichert werden das PDF, seine Prüfsumme und
 * die verwendeten Vorlagenversionen; ein Schreiben wird nie neu gerendert (ADR-007).
 *
 * <p>Jedes Schreiben kennt den Snapshot, aus dem es entstand. Zu einem Snapshot gibt es höchstens ein Schreiben, damit
 * ein wiederholtes Speichern derselben Vorschau kein zweites erzeugt (ADR-021).
 *
 * <p>Ob ein Schreiben noch sichtbar ist, sagt sein {@link LetterState}. Ein logisch gelöschtes behandeln alle
 * Anwendungsfälle wie ein nicht vorhandenes.
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
        this.id = Validate.required(id, "Die ID des Schreibens");
        this.snapshot = Validate.required(snapshot, "Der Snapshot des Schreibens");
        this.reference = Validate.required(reference, "Der fachliche Bezug des Schreibens");
        this.letterhead = Validate.required(letterhead, "Die Briefpapier-Version des Schreibens");
        this.letterTemplate = Validate.required(letterTemplate, "Die Standardschreiben-Version des Schreibens");
        this.document = Validate.required(document, "Der Dokumentverweis des Schreibens");
        this.pdfChecksum = Validate.required(pdfChecksum, "Die Prüfsumme des PDFs");
        this.created = Validate.required(created, "Der Erstellungszeitpunkt des Schreibens");
        this.state = Validate.required(state, "Der Zustand des Schreibens");
    }

    /**
     * Erzeugt das Schreiben aus dem Snapshot, aus dem sein PDF gerendert wurde, und verbraucht ihn. Bezug und
     * Vorlagenversionen kommen aus dem Snapshot, damit das Schreiben nicht von der Vorschau abweichen kann. Aus einem
     * verbrauchten Snapshot entsteht kein weiteres Schreiben (ADR-021).
     *
     * <p>{@code now} ist zugleich Prüfzeitpunkt für den Ablauf und Erstellungszeitpunkt. Der Anwendungsfall übergibt
     * den Zeitpunkt seiner Uhr, damit beides übereinstimmt und Tests die Grenze ohne Warten prüfen können.
     *
     * @throws IllegalStateException wenn der Snapshot zum Zeitpunkt {@code now} abgelaufen oder schon verbraucht ist
     */
    public static LetterFromSnapshot fromSnapshot(
            LetterId id, Snapshot snapshot, DocumentReference document, Checksum pdfChecksum, Instant now) {
        Validate.required(snapshot, "Der Snapshot");
        if (snapshot.isExpired(Validate.required(now, "Der Zeitpunkt"))) {
            throw new IllegalStateException("Snapshot " + snapshot.id().value() + " ist abgelaufen");
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
     * Das Schreiben, wie es nach einer logischen Löschung ist: Das PDF bleibt, sichtbar ist es nicht mehr. Ob gelöscht
     * werden darf und wie, entscheiden Berechtigung und Löschrichtlinie des Hosts, nicht das Schreiben.
     *
     * @throws IllegalStateException wenn das Schreiben schon logisch gelöscht ist
     */
    public Letter logicallyDeleted(Instant now) {
        if (state instanceof LetterState.LogicallyDeleted) {
            throw new IllegalStateException("Schreiben " + id.value() + " ist schon logisch gelöscht");
        }
        return new Letter(id, snapshot, reference, letterhead, letterTemplate, document, pdfChecksum, created,
                new LetterState.LogicallyDeleted(Validate.required(now, "Der Zeitpunkt")));
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

    /** Gleich sind zwei Instanzen mit derselben ID. */
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
        return "Schreiben[id=" + id.value() + ", erstellt=" + created + "]";
    }
}
