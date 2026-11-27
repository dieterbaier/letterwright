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
 * Prüft Snapshot und Berechtigung, bevor es den Zustand des Snapshots auswertet: Ein Unberechtigter erfährt so nicht,
 * ob schon gespeichert wurde. Aus einem offenen Snapshot liest es den Datenkontext erneut, erzeugt das PDF und speichert
 * nur ein konformes (fail-closed). Ein PDF entfernt es nur, wenn sicher ist, dass kein Schreiben darauf verweist.
 *
 * <p>Ein neu angelegtes Schreiben meldet es vor dem Commit dem {@link SaveCallback} des Hosts (ADR-023). Welcher
 * Rückruf gilt, entscheidet die Verdrahtung: Über die Java-API gibt es keinen.
 */
public final class SaveLetterService implements SaveLetter {

    private final SnapshotStore snapshots;
    private final TemplateRepository templates;
    private final Permissions permissions;
    private final PlaceholderDataSource data;
    private final Renderer renderer;
    private final ConformanceChecker checker;
    private final DocumentStore documents;
    private final LetterRepository letter;
    private final SaveCallback callback;
    private final Clock clock;

    public SaveLetterService(SnapshotStore snapshots, TemplateRepository templates, Permissions permissions,
            PlaceholderDataSource data, Renderer renderer, ConformanceChecker checker, DocumentStore documents,
            LetterRepository letter, SaveCallback callback, Clock clock) {
        this.snapshots = snapshots;
        this.templates = templates;
        this.permissions = permissions;
        this.data = data;
        this.renderer = renderer;
        this.checker = checker;
        this.documents = documents;
        this.letter = letter;
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
            // Ist das Schreiben inzwischen gelöscht, gibt es für den Benutzer nichts mehr zu finden.
            case SnapshotState.Consumed consumed -> letter.forSnapshot(id)
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
        LetterFromSnapshot generate =
                Letter.fromSnapshot(LetterId.generate(), snapshot, document, Checksum.of(pdf), now);
        // Wirft speichere, ist der Ausgang ungewiss; das PDF bleibt, weil ein Schreiben darauf verweisen kann.
        return switch (letter.save(generate)) {
            case SaveOutcome.Saved saved -> afterCreation(user, generate.letter());
            case SaveOutcome.SnapshotNoLongerExists removed -> {
                // Eine neue Vorschau hat den Snapshot inzwischen ersetzt; gespeichert wurde sicher nichts.
                documents.remove(document);
                yield new SaveResult.SnapshotUnknown();
            }
            case SaveOutcome.SnapshotAlreadyConsumed already -> {
                // Eine gleichzeitige Anfrage war schneller; ihr PDF gilt, auf dieses verweist sicher nichts.
                documents.remove(document);
                yield already.existing().isVisible()
                        ? new SaveResult.AlreadySaved(already.existing())
                        : new SaveResult.SnapshotUnknown();
            }
            case SaveOutcome.LetterRemoved removed -> {
                // Das Schreiben der schnelleren Anfrage ist schon gelöscht; auf dieses PDF verweist sicher nichts.
                documents.remove(document);
                yield new SaveResult.SnapshotUnknown();
            }
        };
    }

    /**
     * Nur hier entsteht ein neues Schreiben, deshalb läuft der Rückruf nur hier. Lehnt der Host ab, bleibt das PDF
     * liegen: Das Schreiben ist schon geschrieben, und ob der Rollback es zurücknimmt, entscheidet die äußere Grenze.
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

    /** Versionen sind unveränderlich und werden nie gelöscht (ADR-007); fehlt eine, ist der Bestand defekt. */
    private static IllegalStateException missing(VersionReference reference) {
        return new IllegalStateException("Vorlagenversion " + reference + " aus einem Snapshot fehlt");
    }
}
