package dieterbaier.letterwright.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.Checksum;
import dieterbaier.letterwright.domain.model.LetterAction;
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
import dieterbaier.letterwright.usecases.in.SaveResult;
import dieterbaier.letterwright.usecases.in.PreviewResult;
import dieterbaier.letterwright.usecases.out.Conformance;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Bridge to {@code features/letters/save-letter.feature} and the first four scenarios of
 * {@code features/letters/letter-save-callback.feature}: one test per scenario, named after the scenario title. The
 * preview is created with the real use case so that snapshot and save fit together; the outbound ports are
 * {@link Fakes}.
 */
class SaveLetterServiceTest {

    private static final Instant PREVIEW = Instant.parse("2026-11-22T10:00:00Z");
    private static final UserId ERIKA = new UserId("erika");
    private static final UserId MAX = new UserId("max");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Customer", "4711");

    private final Fakes.Templates templates = new Fakes.Templates();
    private final Fakes.Host host = new Fakes.Host();
    private final Fakes.Snapshots snapshots = new Fakes.Snapshots();
    private final Fakes.Renderer renderer = new Fakes.Renderer();
    private final Fakes.Checker checker = new Fakes.Checker();
    private final Fakes.Documents documents = new Fakes.Documents();
    private final Fakes.LetterStore letters = new Fakes.LetterStore(snapshots);
    private final Fakes.Callback callback = new Fakes.Callback();
    private final Fakes.StoppedClock clock = new Fakes.StoppedClock(PREVIEW);
    private final CreatePreviewService preview = new CreatePreviewService(templates, host, host, renderer, snapshots, clock);
    private final SaveLetterService saving = new SaveLetterService(snapshots, templates, host, host, renderer,
            checker, documents, letters, callback, clock);

    private final Letterhead letterhead = templates.store(
            Letterhead.createWithDefaultType(TemplateId.generate(), "Standard", "<header/>").publishDraft());
    private final LetterTemplate reminder = templates.store(LetterTemplate.create(TemplateId.generate(), "Payment reminder",
            letterhead.id(), "Customer", "<p>{{name}}</p>").publishDraft());

    // Background: Erika created a preview 10 minutes ago, may create, and the host provides the same data.
    private final SnapshotId previewOfErika = createPreview(ERIKA);

    {
        clock.now = PREVIEW.plus(Duration.ofMinutes(10));
        host.creationRequested.clear();
        host.dataContextRequested = 0;
    }

    @Test
    void anUnchangedPreviewSavesTheCheckedPdf() {
        var result = saving.save(ERIKA, previewOfErika);

        Letter saved = letters.saved.get(previewOfErika);
        assertThat(result).isEqualTo(new SaveResult.Saved(saved));
        Snapshot snapshot = snapshots.load(previewOfErika).orElseThrow();
        assertThat(snapshot.state()).as("data context released").isEqualTo(new SnapshotState.Consumed());
        assertThat(saved.letterhead()).isEqualTo(snapshot.letterhead());
        assertThat(saved.letterTemplate()).isEqualTo(snapshot.letterTemplate());
        assertThat(saved.reference()).isEqualTo(CUSTOMER_4711);
        assertThat(saved.created()).isEqualTo(clock.now);
        byte[] pdf = documents.stored.get(saved.document());
        assertThat(new String(pdf, StandardCharsets.UTF_8)).isEqualTo("<header/><p>Erika Mustermann GmbH</p>");
        assertThat(saved.pdfChecksum()).isEqualTo(Checksum.of(pdf));
    }

    @Test
    void ifThePermissionIsRevokedWhileSavingNothingIsPersisted() {
        host.allowed = false;

        var result = saving.save(ERIKA, previewOfErika);

        assertThat(result).isInstanceOf(SaveResult.NotAllowed.class);
        assertThat(host.creationRequested).containsExactly(reminder);
        assertThat(renderer.pdfs).isZero();
        nothingSavedOrStored();
    }

    @Test
    void aNonConformantPdfIsNotSaved() {
        checker.result = pdf -> Conformance.notConformant(List.of("PDF/UA-1: Image without alternative text"));

        var result = saving.save(ERIKA, previewOfErika);

        assertThat(result)
                .isEqualTo(new SaveResult.NotConformant(List.of("PDF/UA-1: Image without alternative text")));
        nothingSavedOrStored();
    }

    @Test
    void anUncheckablePdfIsNotSaved() {
        checker.result = pdf -> {
            throw new IllegalStateException("veraPDF not reachable");
        };

        assertThatThrownBy(() -> saving.save(ERIKA, previewOfErika))
                .hasMessageContaining("veraPDF not reachable");
        nothingSavedOrStored();
    }

    @Test
    void afterAChangeOfTheDataContextNoPdfIsCreated() {
        host.dataContext = DataContext.from(Map.of("name", "Erika Musterfrau AG"));

        var result = saving.save(ERIKA, previewOfErika);

        assertThat(result).isInstanceOf(SaveResult.DataContextChanged.class);
        assertThat(renderer.pdfs).isZero();
        nothingSavedOrStored();
    }

    @Test
    void afterANewPreviewSavingSucceeds() {
        host.dataContext = DataContext.from(Map.of("name", "Erika Musterfrau AG"));
        var updated = (PreviewResult.Created) preview.update(ERIKA, CUSTOMER_4711, reminder.id(),
                previewOfErika);

        var result = saving.save(ERIKA, updated.snapshot());

        assertThat(result).isInstanceOf(SaveResult.Saved.class);
        Letter saved = letters.saved.get(updated.snapshot());
        assertThat(new String(documents.stored.get(saved.document()), StandardCharsets.UTF_8))
                .contains("Erika Musterfrau AG");
    }

    @Test
    void aForeignSnapshotBehavesLikeAMissingOne() {
        assertThat(saving.save(MAX, previewOfErika)).isInstanceOf(SaveResult.SnapshotUnknown.class);
        assertThat(saving.save(ERIKA, SnapshotId.generate())).isInstanceOf(SaveResult.SnapshotUnknown.class);
        assertThat(host.wasAsked()).isFalse();
    }

    @Test
    void aForeignSnapshotStaysHiddenEvenAfterSaving() {
        saving.save(ERIKA, previewOfErika);

        assertThat(saving.save(MAX, previewOfErika)).isInstanceOf(SaveResult.SnapshotUnknown.class);
    }

    @Test
    void anExpiredSnapshotIsNotSaved() {
        clock.now = PREVIEW.plus(Duration.ofMinutes(30));

        assertThat(saving.save(ERIKA, previewOfErika))
                .isInstanceOf(SaveResult.SnapshotExpired.class);
        assertThat(host.wasAsked()).isFalse();
        nothingSavedOrStored();
    }

    @Test
    void repeatedSavingOfTheSamePreviewYieldsNoSecondLetter() {
        var first = (SaveResult.Saved) saving.save(ERIKA, previewOfErika);
        host.dataContextRequested = 0;

        var again = saving.save(ERIKA, previewOfErika);

        // Then: the same letter, without data context and without a new PDF
        assertThat(again).isEqualTo(new SaveResult.AlreadySaved(first.letter()));
        assertThat(host.dataContextRequested).isZero();
        assertThat(renderer.pdfs).isEqualTo(1);
        assertThat(documents.stored).hasSize(1);
    }

    @Test
    void ifTheOutcomeOfSavingIsUncertainThePdfIsKept() {
        // Given: the persistence saves, but the acknowledgement is lost
        letters.after = Optional.of(new IllegalStateException("Connection lost"));

        // When: Erika saves; the save fails with an error
        assertThatThrownBy(() -> saving.save(ERIKA, previewOfErika))
                .hasMessageContaining("Connection lost");

        // Then: the PDF is still stored; a retry returns the letter with exactly this PDF
        letters.after = Optional.empty();
        var again = (SaveResult.AlreadySaved) saving.save(ERIKA, previewOfErika);
        Letter saved = letters.saved.get(previewOfErika);
        assertThat(again.letter()).isEqualTo(saved);
        assertThat(documents.stored).containsOnlyKeys(saved.document());
    }

    @Test
    void ifErikaSavesWhileUpdatingThePreviewARepetitionFindsTheLetter() {
        // Given: the update has read the open snapshot; before the deletion, another request saves
        SaveResult[] concurrent = new SaveResult[1];
        snapshots.beforeDeletion = () -> concurrent[0] = saving.save(ERIKA, previewOfErika);

        // When: the update wants to delete the previous snapshot
        preview.update(ERIKA, CUSTOMER_4711, reminder.id(), previewOfErika);

        // Then: the consumed snapshot stays; a retry of the save finds the letter
        var saved = (SaveResult.Saved) concurrent[0];
        assertThat(snapshots.deleted).isEmpty();
        assertThat(saving.save(ERIKA, previewOfErika))
                .isEqualTo(new SaveResult.AlreadySaved(saved.letter()));
    }

    @Test
    void ifAConcurrentPreviewReplacesTheSnapshotNothingIsSaved() {
        // Given: while Erika saves, she replaces the preview in another tab
        letters.before = () -> preview.update(ERIKA, CUSTOMER_4711, reminder.id(), previewOfErika);

        var result = saving.save(ERIKA, previewOfErika);

        // Then: no letter, no orphaned PDF; saving happens from the new preview
        assertThat(result).isInstanceOf(SaveResult.SnapshotUnknown.class);
        assertThat(letters.saved).isEmpty();
        assertThat(documents.stored).isEmpty();
    }

    @Test
    void concurrentSavingOfTheSamePreviewYieldsOneLetter() {
        // Given: a second request consumes the snapshot while this one generates its PDF
        LetterFromSnapshot ofTheSecondRequest = Letter.fromSnapshot(LetterId.generate(),
                snapshots.load(previewOfErika).orElseThrow(), new DocumentReference("store/second"),
                Checksum.of(new byte[] {1}), clock.now);
        letters.before = () -> letters.adopt(ofTheSecondRequest);

        var result = saving.save(ERIKA, previewOfErika);

        assertThat(result).isEqualTo(new SaveResult.AlreadySaved(ofTheSecondRequest.letter()));
        assertThat(letters.saved).containsOnly(Map.entry(previewOfErika, ofTheSecondRequest.letter()));
        assertThat(documents.stored).isEmpty();
    }

    @Test
    void aNewPreviewYieldsANewLetter() {
        var first = (SaveResult.Saved) saving.save(ERIKA, previewOfErika);
        SnapshotId newPreview = createPreview(ERIKA);

        var second = saving.save(ERIKA, newPreview);

        assertThat(second).isInstanceOf(SaveResult.Saved.class).isNotEqualTo(first);
        assertThat(letters.saved).hasSize(2);
    }

    @Test
    void withoutPermissionItStaysHiddenWhetherItWasAlreadySaved() {
        saving.save(ERIKA, previewOfErika);
        host.allowed = false;

        assertThat(saving.save(ERIKA, previewOfErika)).isInstanceOf(SaveResult.NotAllowed.class);
    }

    @ParameterizedTest(name = "{0} deleted")
    @ValueSource(strings = {"hard", "logical"})
    void ifTheLetterIsDeletedARepeatedSaveFindsNothing(String kind) {
        var saved = (SaveResult.Saved) saving.save(ERIKA, previewOfErika);
        delete(saved.letter().id(), kind);

        var again = saving.save(ERIKA, previewOfErika);

        assertThat(again).isInstanceOf(SaveResult.SnapshotUnknown.class);
        assertThat(renderer.pdfs).as("no further PDF").isEqualTo(1);
    }

    @ParameterizedTest(name = "{0} deleted")
    @ValueSource(strings = {"hard", "logical"})
    void ifTheLetterOfAConcurrentRequestIsDeletedNothingIsSaved(String kind) {
        // Given: a second request saves the same preview, and its letter is deleted before this one saves
        byte[] pdfOfTheSecond = {1};
        LetterFromSnapshot ofTheSecondRequest = Letter.fromSnapshot(LetterId.generate(),
                snapshots.load(previewOfErika).orElseThrow(), documents.put(pdfOfTheSecond),
                Checksum.of(pdfOfTheSecond), clock.now);
        letters.before = () -> {
            letters.adopt(ofTheSecondRequest);
            delete(ofTheSecondRequest.letter().id(), kind);
        };

        var result = saving.save(ERIKA, previewOfErika);

        // Then: no letter, and its own PDF is removed again
        assertThat(result).isInstanceOf(SaveResult.SnapshotUnknown.class);
        assertThat(letters.saved.values()).noneMatch(Letter::isVisible);
        assertThat(documents.stored.keySet()).as("only the PDF of a logically deleted letter remains")
                .isSubsetOf(ofTheSecondRequest.letter().document());
    }

    // Letter save callback (letter-save-callback.feature)

    @Test
    void ifSavingCreatesALetterTheHostReceivesACallback() {
        var result = saving.save(ERIKA, previewOfErika);

        Letter saved = letters.saved.get(previewOfErika);
        assertThat(result).isEqualTo(new SaveResult.Saved(saved));
        assertThat(callback.invocations).containsExactly(new Fakes.CallbackInvocation(ERIKA, saved));
    }

    @Test
    void aRepetitionTriggersNoFurtherCallback() {
        var first = (SaveResult.Saved) saving.save(ERIKA, previewOfErika);

        var again = saving.save(ERIKA, previewOfErika);

        assertThat(again).isEqualTo(new SaveResult.AlreadySaved(first.letter()));
        assertThat(callback.invocations).hasSize(1);
    }

    @Test
    void theHostRejectsTheNewLetterWithAReason() {
        callback.result = newOne -> CallbackResult.rejected("The task is already done");

        var result = saving.save(ERIKA, previewOfErika);

        assertThat(result).isEqualTo(new SaveResult.RejectedByHost("The task is already done"));
        assertThat(callback.invocations).hasSize(1);
    }

    @Test
    void anErrorInTheCallbackIsATechnicalError() {
        callback.result = newOne -> {
            throw new IllegalStateException("Task management not reachable");
        };

        assertThatThrownBy(() -> saving.save(ERIKA, previewOfErika))
                .isInstanceOf(IllegalStateException.class).hasMessage("Task management not reachable");
        assertThat(callback.invocations).as("not repeated").hasSize(1);
    }

    @Test
    void ifNothingIsCreatedThereIsNoCallback() {
        checker.result = pdf -> Conformance.notConformant(List.of("PDF/UA-1: Image without alternative text"));
        saving.save(ERIKA, previewOfErika);
        host.allowed = false;
        saving.save(ERIKA, previewOfErika);

        assertThat(callback.invocations).isEmpty();
    }

    private void delete(LetterId id, String kind) {
        var policy = new Fakes.DeletionPolicy();
        policy.decision = kind.equals("hard") ? DeletionDecision.hard() : DeletionDecision.logical();
        host.allow(ERIKA, LetterAction.DELETE, CUSTOMER_4711);
        new DeleteLetterService(letters, host, policy, documents, clock).delete(ERIKA, id);
    }

    @Test
    void aMissingTemplateVersionIsCorruptData() {
        templates.letterheads.clear();

        assertThatThrownBy(() -> saving.save(ERIKA, previewOfErika))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("missing");
        templates.letterTemplates.clear();
        assertThatThrownBy(() -> saving.save(ERIKA, previewOfErika))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("missing");
    }

    private SnapshotId createPreview(UserId user) {
        host.allowed = true;
        return ((PreviewResult.Created) preview.create(user, CUSTOMER_4711, reminder.id())).snapshot();
    }

    /** The snapshot also stays open and keeps its data context for another attempt. */
    private void nothingSavedOrStored() {
        assertThat(letters.saved).isEmpty();
        assertThat(documents.stored).isEmpty();
        assertThat(snapshots.load(previewOfErika).orElseThrow().state())
                .isInstanceOf(SnapshotState.Open.class);
    }
}
