package dieterbaier.letterwright.usecases.service;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.LetterAction;
import dieterbaier.letterwright.domain.model.Page;
import dieterbaier.letterwright.domain.model.PageRequest;
import dieterbaier.letterwright.domain.model.TemplateAction;
import dieterbaier.letterwright.domain.model.letter.DocumentReference;
import dieterbaier.letterwright.domain.model.letter.DeletionDecision;
import dieterbaier.letterwright.domain.model.letter.CallbackResult;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.domain.model.letter.LetterFromSnapshot;
import dieterbaier.letterwright.domain.model.letter.LetterId;
import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotState;
import dieterbaier.letterwright.domain.model.template.Letterhead;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import dieterbaier.letterwright.domain.model.template.TemplateVersion;
import dieterbaier.letterwright.usecases.out.Permissions;
import dieterbaier.letterwright.usecases.out.DocumentStore;
import dieterbaier.letterwright.usecases.out.ReplaceOutcome;
import dieterbaier.letterwright.usecases.out.Conformance;
import dieterbaier.letterwright.usecases.out.ConformanceChecker;
import dieterbaier.letterwright.usecases.out.DeletionRules;
import dieterbaier.letterwright.usecases.out.PlaceholderDataSource;
import dieterbaier.letterwright.usecases.out.LetterRepository;
import dieterbaier.letterwright.usecases.out.SnapshotStore;
import dieterbaier.letterwright.usecases.out.SaveOutcome;
import dieterbaier.letterwright.usecases.out.SaveCallback;
import dieterbaier.letterwright.usecases.out.TemplateRepository;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/** Fakes of the outbound ports for the use case tests. They record what was asked. */
final class Fakes {

    private Fakes() {
    }

    static final class Templates implements TemplateRepository {

        final Map<TemplateId, LetterTemplate> letterTemplates = new HashMap<>();
        final Map<TemplateId, Letterhead> letterheads = new HashMap<>();

        LetterTemplate store(LetterTemplate template) {
            letterTemplates.put(template.id(), template);
            return template;
        }

        Letterhead store(Letterhead template) {
            letterheads.put(template.id(), template);
            return template;
        }

        @Override
        public Optional<LetterTemplate> letterTemplate(TemplateId id) {
            return Optional.ofNullable(letterTemplates.get(id));
        }

        /** Runs immediately before the conditional replacement, for example as a concurrent change by another user. */
        Runnable before = () -> { };
        int replacements;

        @Override
        public Optional<Letterhead> letterhead(TemplateId id) {
            return Optional.ofNullable(letterheads.get(id));
        }

        @Override
        public ReplaceOutcome replace(LetterTemplate loaded, LetterTemplate changed) {
            before.run();
            LetterTemplate saved = letterTemplates.get(loaded.id());
            if (saved == null || !state(saved.versions()).equals(state(loaded.versions()))) {
                return new ReplaceOutcome.Stale();
            }
            replacements++;
            store(changed);
            return new ReplaceOutcome.Replaced();
        }

        @Override
        public ReplaceOutcome replace(Letterhead loaded, Letterhead changed) {
            before.run();
            Letterhead saved = letterheads.get(loaded.id());
            if (saved == null || !state(saved.versions()).equals(state(loaded.versions()))) {
                return new ReplaceOutcome.Stale();
            }
            replacements++;
            store(changed);
            return new ReplaceOutcome.Replaced();
        }

        /** Template versions are equal by their reference; the comparison needs the whole state. */
        private static List<List<Object>> state(List<TemplateVersion> versions) {
            return versions.stream()
                    .map(version -> List.<Object>of(version.reference(), version.state(), version.content(),
                            version.sampleData()))
                    .toList();
        }
    }

    static final class Host implements Permissions, PlaceholderDataSource {

        DataContext dataContext = DataContext.from(Map.of("name", "Erika Mustermann GmbH"));
        final List<LetterTemplate> creationRequested = new ArrayList<>();
        final List<Grant> letterActionRequested = new ArrayList<>();
        final Set<Grant> letterActions = new java.util.HashSet<>();
        final Set<UserId> mayPublish = new java.util.HashSet<>();
        final List<TemplateAction> templateActionRequested = new ArrayList<>();
        boolean allowed;
        int dataContextRequested;

        boolean wasAsked() {
            return !creationRequested.isEmpty() || dataContextRequested > 0 || !letterActionRequested.isEmpty()
                    || !templateActionRequested.isEmpty();
        }

        void allow(UserId user, LetterAction action, BusinessReference reference) {
            letterActions.add(new Grant(user, action, reference));
        }

        void deny(UserId user, LetterAction action, BusinessReference reference) {
            letterActions.remove(new Grant(user, action, reference));
        }

        /** Only publishing is allowed, and only to the users in {@link #mayPublish}. */
        @Override
        public boolean may(UserId user, TemplateAction action) {
            templateActionRequested.add(action);
            return action == TemplateAction.PUBLISH && mayPublish.contains(user);
        }

        /** Allowed is what {@link #allow} has granted. */
        @Override
        public boolean may(UserId user, LetterAction action, BusinessReference reference) {
            Grant request = new Grant(user, action, reference);
            letterActionRequested.add(request);
            return letterActions.contains(request);
        }

        @Override
        public boolean mayCreate(UserId user, BusinessReference reference, LetterTemplate template) {
            creationRequested.add(template);
            return allowed;
        }

        @Override
        public DataContext readDataContext(UserId user, BusinessReference reference, LetterTemplate template) {
            dataContextRequested++;
            return dataContext;
        }
    }

    record Grant(UserId user, LetterAction action, BusinessReference reference) {}

    /** Answers with the given decision and records what was asked. */
    static final class DeletionPolicy implements DeletionRules {

        DeletionDecision decision = DeletionDecision.hard();
        final List<Letter> requested = new ArrayList<>();

        @Override
        public DeletionDecision decide(UserId user, Letter letter) {
            requested.add(letter);
            return decision;
        }
    }

    /** Replaces only {@code {{name}}}; {@code adapter-rendering} tests how the real merge works. */
    static final class Renderer implements dieterbaier.letterwright.usecases.out.Renderer {

        int invocations;
        int pdfs;
        final List<DataContext> pdfData = new ArrayList<>();

        @Override
        public String merge(TemplateVersion letterhead, TemplateVersion content, DataContext data) {
            invocations++;
            return letterhead.content() + content.content().replace("{{name}}", String.valueOf(data.values().get("name")));
        }

        /** The "PDF" is the HTML as bytes; this way a test sees which versions and data it was created from. */
        @Override
        public byte[] createPdf(TemplateVersion letterhead, TemplateVersion content, DataContext data) {
            pdfs++;
            pdfData.add(data);
            return merge(letterhead, content, data).getBytes(StandardCharsets.UTF_8);
        }

        /** Like {@link #createPdf}, with "Sample content" in place of the letter template. */
        @Override
        public byte[] createProofPdf(TemplateVersion letterhead, DataContext sampleData) {
            pdfs++;
            pdfData.add(sampleData);
            return (letterhead.content() + "Sample content").getBytes(StandardCharsets.UTF_8);
        }
    }

    static final class Snapshots implements SnapshotStore {

        final Map<SnapshotId, Snapshot> snapshots = new HashMap<>();
        final List<SnapshotId> deleted = new ArrayList<>();
        /** Runs before the conditional deletion and simulates a concurrent request. */
        Runnable beforeDeletion = () -> { };

        @Override
        public void save(Snapshot snapshot) {
            snapshots.put(snapshot.id(), snapshot);
        }

        @Override
        public Optional<Snapshot> load(SnapshotId id) {
            return Optional.ofNullable(snapshots.get(id));
        }

        @Override
        public void deleteIfOpen(SnapshotId id) {
            beforeDeletion.run();
            Snapshot current = snapshots.get(id);
            if (current != null && current.state() instanceof SnapshotState.Open) {
                snapshots.remove(id);
                deleted.add(id);
            }
        }
    }

    static final class StoppedClock extends Clock {

        Instant now;

        StoppedClock(Instant now) {
            this.now = now;
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            throw new UnsupportedOperationException();
        }
    }

    /** Records every callback; by default the host accepts every letter. */
    static final class Callback implements SaveCallback {

        final List<CallbackInvocation> invocations = new ArrayList<>();
        Function<Letter, CallbackResult> result = letter -> CallbackResult.accepted();

        @Override
        public CallbackResult created(UserId user, Letter letter) {
            invocations.add(new CallbackInvocation(user, letter));
            return result.apply(letter);
        }
    }

    record CallbackInvocation(UserId user, Letter letter) {}

    /** A configurable result; by default every PDF is conformant. */
    static final class Checker implements ConformanceChecker {

        Function<byte[], Conformance> result = pdf -> Conformance.conformant();

        @Override
        public Conformance check(byte[] pdf) {
            return result.apply(pdf);
        }
    }

    static final class Documents implements DocumentStore {

        final Map<DocumentReference, byte[]> stored = new LinkedHashMap<>();
        int number;
        int read;

        @Override
        public DocumentReference put(byte[] pdf) {
            var reference = new DocumentReference("store/" + ++number);
            stored.put(reference, pdf);
            return reference;
        }

        @Override
        public byte[] read(DocumentReference document) {
            read++;
            byte[] pdf = stored.get(document);
            if (pdf == null) {
                throw new IllegalStateException("No PDF for " + document.value());
            }
            return pdf.clone();
        }

        @Override
        public void remove(DocumentReference document) {
            stored.remove(document);
        }
    }

    /**
     * Behaves like the persistence: saving the letter and consuming the snapshot happen in one step and only with an
     * open snapshot. {@link #before} runs before this step and simulates a concurrent request; {@link #after} throws
     * after saving, as if the acknowledgement were lost.
     */
    static final class LetterStore implements LetterRepository {

        final Map<SnapshotId, Letter> saved = new HashMap<>();
        private final Snapshots snapshots;
        Runnable before = () -> { };
        Optional<RuntimeException> after = Optional.empty();
        int query;

        LetterStore(Snapshots snapshots) {
            this.snapshots = snapshots;
        }

        /** Stores a letter as if it had been saved earlier. */
        Letter store(Letter letter) {
            saved.put(letter.snapshot(), letter);
            return letter;
        }

        Optional<Letter> withId(LetterId id) {
            return saved.values().stream().filter(letter -> letter.id().equals(id)).findFirst();
        }

        @Override
        public Optional<Letter> load(LetterId id) {
            return withId(id);
        }

        @Override
        public Optional<Letter> forSnapshot(SnapshotId snapshot) {
            return Optional.ofNullable(saved.get(snapshot));
        }

        /** Filters like the database query, before paginating and counting. */
        @Override
        public Page<Letter> visibleFor(BusinessReference reference, PageRequest request) {
            query++;
            List<Letter> visible = saved.values().stream()
                    .filter(letter -> letter.reference().equals(reference))
                    .filter(Letter::isVisible)
                    .sorted(Comparator.comparing(Letter::created).reversed())
                    .toList();
            return new Page<>(visible.stream().skip(request.offset()).limit(request.size()).toList(),
                    visible.size(), request);
        }

        @Override
        public void replace(Letter changed) {
            saved.put(changed.snapshot(), changed);
        }

        @Override
        public void remove(LetterId id) {
            withId(id).ifPresent(letter -> saved.remove(letter.snapshot()));
        }

        @Override
        public SaveOutcome save(LetterFromSnapshot creation) {
            before.run();
            SaveOutcome outcome = adopt(creation);
            after.ifPresent(error -> {
                throw error;
            });
            return outcome;
        }

        /** The atomic step itself, without the simulated failures. */
        SaveOutcome adopt(LetterFromSnapshot creation) {
            SnapshotId id = creation.consumedSnapshot().id();
            Snapshot current = snapshots.snapshots.get(id);
            if (current == null) {
                return new SaveOutcome.SnapshotNoLongerExists();
            }
            if (current.state() instanceof SnapshotState.Consumed) {
                Letter existing = saved.get(id);
                return existing == null
                        ? new SaveOutcome.LetterRemoved()
                        : new SaveOutcome.SnapshotAlreadyConsumed(existing);
            }
            saved.put(id, creation.letter());
            snapshots.snapshots.put(id, creation.consumedSnapshot());
            return new SaveOutcome.Saved();
        }
    }
}
