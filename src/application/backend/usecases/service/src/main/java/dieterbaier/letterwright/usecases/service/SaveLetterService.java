package dieterbaier.letterwright.usecases.service;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.letter.DocumentReference;
import dieterbaier.letterwright.domain.model.letter.CallbackResult;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterFromSnapshot;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotState;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateVersion;
import dieterbaier.letterwright.usecases.in.SaveLetter;
import dieterbaier.letterwright.usecases.in.SaveResult;
import dieterbaier.letterwright.usecases.out.Permissions;
import dieterbaier.letterwright.usecases.out.DocumentStore;
import dieterbaier.letterwright.usecases.out.Conformance;
import dieterbaier.letterwright.usecases.out.ConformanceChecker;
import dieterbaier.letterwright.usecases.out.PlaceholderDataSource;
import dieterbaier.letterwright.usecases.out.Renderer;
import dieterbaier.letterwright.usecases.out.LetterRepository;
import dieterbaier.letterwright.usecases.out.SaveOutcome;
import dieterbaier.letterwright.usecases.out.SaveCallback;
import dieterbaier.letterwright.usecases.out.SnapshotStore;
import dieterbaier.letterwright.usecases.out.TemplateRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Checks snapshot and permission before evaluating the snapshot's state, so that an unauthorized user does not learn
 * whether it was already saved. From an open snapshot it reads the data context again, generates the PDF and saves only
 * a conformant one (fail-closed). It removes a PDF only when it is certain that no letter refers to it.
 *
 * <p>It reports a newly created letter to the host's {@link SaveCallback} before the commit (ADR-023). The wiring
 * decides which callback applies: via the Java API there is none.
 */
public final class SaveLetterService implements SaveLetter {

    private final SnapshotStore snapshots;
    private final TemplateRepository templates;
    private final Permissions permissions;
    private final PlaceholderDataSource data;
    private final Renderer renderer;
    private final ConformanceChecker checker;
    private final DocumentStore documents;
    private final LetterRepository letters;
    private final SaveCallback callback;
    private final Clock clock;

    public SaveLetterService(SnapshotStore snapshots, TemplateRepository templates, Permissions permissions,
            PlaceholderDataSource data, Renderer renderer, ConformanceChecker checker, DocumentStore documents,
            LetterRepository letters, SaveCallback callback, Clock clock) {
        this.snapshots = snapshots;
        this.templates = templates;
        this.permissions = permissions;
        this.data = data;
        this.renderer = renderer;
        this.checker = checker;
        this.documents = documents;
        this.letters = letters;
        this.callback = callback;
        this.clock = clock;
    }

    @Override
    public SaveResult save(UserId user, SnapshotId id) {
        Optional<Snapshot> own = snapshots.load(id).filter(snapshot -> snapshot.user().equals(user));
        if (own.isEmpty()) {
            return new SaveResult.SnapshotUnknown();
        }
        Snapshot snapshot = own.get();
        Instant now = clock.instant();
        if (snapshot.isExpired(now)) {
            return new SaveResult.SnapshotExpired();
        }
        LetterTemplate template = templates.letterTemplate(snapshot.letterTemplate().template())
                .orElseThrow(() -> missing(snapshot.letterTemplate()));
        if (!permissions.mayCreate(user, snapshot.reference(), template)) {
            return new SaveResult.NotAllowed();
        }
        return switch (snapshot.state()) {
            // If the letter has since been deleted, there is nothing left for the user to find.
            case SnapshotState.Consumed consumed -> letters.forSnapshot(id)
                    .filter(Letter::isVisible)
                    .<SaveResult>map(existing -> new SaveResult.AlreadySaved(existing))
                    .orElseGet(SaveResult.SnapshotUnknown::new);
            case SnapshotState.Open open -> save(user, snapshot, open, template, now);
        };
    }

    private SaveResult save(UserId user, Snapshot snapshot, SnapshotState.Open open,
            LetterTemplate template, Instant now) {
        if (!open.isUnchanged(data.readDataContext(user, snapshot.reference(), template))) {
            return new SaveResult.DataContextChanged();
        }
        byte[] pdf = renderer.createPdf(letterheadVersion(snapshot), template.version(snapshot.letterTemplate()
                .number()).orElseThrow(() -> missing(snapshot.letterTemplate())), open.dataContext());
        return switch (checker.check(pdf)) {
            case Conformance.NotConformant notConformant -> new SaveResult.NotConformant(notConformant.findings());
            case Conformance.Conformant conformant -> save(user, snapshot, pdf, now);
        };
    }

    private SaveResult save(UserId user, Snapshot snapshot, byte[] pdf, Instant now) {
        DocumentReference document = documents.put(pdf);
        LetterFromSnapshot creation =
                Letter.fromSnapshot(LetterId.generate(), snapshot, document, Checksum.of(pdf), now);
        // If save throws, the outcome is uncertain; the PDF stays because a letter may refer to it.
        return switch (letters.save(creation)) {
            case SaveOutcome.Saved saved -> afterCreation(user, creation.letter());
            case SaveOutcome.SnapshotNoLongerExists removed -> {
                // A new preview has replaced the snapshot in the meantime; nothing was saved for certain.
                documents.remove(document);
                yield new SaveResult.SnapshotUnknown();
            }
            case SaveOutcome.SnapshotAlreadyConsumed already -> {
                // A concurrent request was faster; its PDF counts, nothing refers to this one for certain.
                documents.remove(document);
                yield already.existing().isVisible()
                        ? new SaveResult.AlreadySaved(already.existing())
                        : new SaveResult.SnapshotUnknown();
            }
            case SaveOutcome.LetterRemoved removed -> {
                // The letter of the faster request is already deleted; nothing refers to this PDF for certain.
                documents.remove(document);
                yield new SaveResult.SnapshotUnknown();
            }
        };
    }

    /**
     * Only here is a new letter created, so the callback runs only here. If the host rejects it, the PDF stays: the
     * letter is already written, and the outer boundary decides whether the rollback undoes it.
     */
    private SaveResult afterCreation(UserId user, Letter newOne) {
        return switch (callback.created(user, newOne)) {
            case CallbackResult.Accepted accepted -> new SaveResult.Saved(newOne);
            case CallbackResult.Rejected rejected -> new SaveResult.RejectedByHost(rejected.reason());
        };
    }

    private TemplateVersion letterheadVersion(Snapshot snapshot) {
        VersionReference reference = snapshot.letterhead();
        return templates.letterhead(reference.template()).flatMap(letterhead -> letterhead.version(reference.number()))
                .orElseThrow(() -> missing(reference));
    }

    /** Versions are immutable and never deleted (ADR-007); if one is missing, the stored data is corrupt. */
    private static IllegalStateException missing(VersionReference reference) {
        return new IllegalStateException("Template version " + reference + " from a snapshot is missing");
    }
}
