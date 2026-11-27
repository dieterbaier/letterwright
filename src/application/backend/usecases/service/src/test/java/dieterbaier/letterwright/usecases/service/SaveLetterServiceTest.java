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
 * Brücke zu {@code features/letter/letter-speichern.feature} und den ersten vier Szenarien von
 * {@code features/letter/callback-nach-dem-speichern.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die Vorschau entsteht mit dem echten Anwendungsfall, damit Snapshot und Speichern zusammenpassen; die
 * ausgehenden Ports sind {@link Fakes}.
 */
class SaveLetterServiceTest {

    private static final Instant PREVIEW = Instant.parse("2026-11-22T10:00:00Z");
    private static final UserId ERIKA = new UserId("erika");
    private static final UserId MAX = new UserId("max");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Kunde", "4711");

    private final Fakes.Templates templates = new Fakes.Templates();
    private final Fakes.Host host = new Fakes.Host();
    private final Fakes.Snapshots snapshots = new Fakes.Snapshots();
    private final Fakes.Renderer renderer = new Fakes.Renderer();
    private final Fakes.Checker checker = new Fakes.Checker();
    private final Fakes.Documents documents = new Fakes.Documents();
    private final Fakes.LetterStore letter = new Fakes.LetterStore(snapshots);
    private final Fakes.Callback callback = new Fakes.Callback();
    private final Fakes.StoppedClock clock = new Fakes.StoppedClock(PREVIEW);
    private final CreatePreviewService preview = new CreatePreviewService(templates, host, host, renderer, snapshots, clock);
    private final SaveLetterService saving = new SaveLetterService(snapshots, templates, host, host, renderer,
            checker, documents, letter, callback, clock);

    private final Letterhead letterhead = templates.store(
            Letterhead.createWithDefaultType(TemplateId.generate(), "Standard", "<header/>").publishDraft());
    private final LetterTemplate reminder = templates.store(LetterTemplate.create(TemplateId.generate(), "Mahnung",
            letterhead.id(), "Kunde", "<p>{{name}}</p>").publishDraft());

    // Grundlage: Erika hat vor 10 Minuten eine Vorschau erzeugt, darf erzeugen, der Host liefert dieselben Daten.
    private final SnapshotId previewOfErika = createPreview(ERIKA);

    {
        clock.now = PREVIEW.plus(Duration.ofMinutes(10));
        host.creationRequested.clear();
        host.dataContextRequested = 0;
    }

    @Test
    void anUnchangedPreviewSavesTheCheckedPdf() {
        var result = saving.save(ERIKA, previewOfErika);

        Letter saved = letter.saved.get(previewOfErika);
        assertThat(result).isEqualTo(new SaveResult.Saved(saved));
        Snapshot snapshot = snapshots.load(previewOfErika).orElseThrow();
        assertThat(snapshot.state()).as("Datenkontext freigegeben").isEqualTo(new SnapshotState.Consumed());
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
        checker.result = pdf -> Conformance.notConformant(List.of("PDF/UA-1: Bild ohne Alternativtext"));

        var result = saving.save(ERIKA, previewOfErika);

        assertThat(result)
                .isEqualTo(new SaveResult.NotConformant(List.of("PDF/UA-1: Bild ohne Alternativtext")));
        nothingSavedOrStored();
    }

    @Test
    void anUncheckablePdfIsNotSaved() {
        checker.result = pdf -> {
            throw new IllegalStateException("veraPDF nicht erreichbar");
        };

        assertThatThrownBy(() -> saving.save(ERIKA, previewOfErika))
                .hasMessageContaining("veraPDF nicht erreichbar");
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
        Letter saved = letter.saved.get(updated.snapshot());
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

        // Then: dasselbe Schreiben, ohne Datenkontext und ohne neues PDF
        assertThat(again).isEqualTo(new SaveResult.AlreadySaved(first.letter()));
        assertThat(host.dataContextRequested).isZero();
        assertThat(renderer.pdfs).isEqualTo(1);
        assertThat(documents.stored).hasSize(1);
    }

    @Test
    void ifTheOutcomeOfSavingIsUncertainThePdfIsKept() {
        // Given: Die Persistenz speichert, aber die Bestätigung geht verloren
        letter.after = Optional.of(new IllegalStateException("Verbindung abgebrochen"));

        // When: Erika speichert; das Speichern schlägt mit einem Fehler fehl
        assertThatThrownBy(() -> saving.save(ERIKA, previewOfErika))
                .hasMessageContaining("Verbindung abgebrochen");

        // Then: Das PDF ist noch abgelegt; eine Wiederholung liefert das Schreiben mit genau diesem PDF
        letter.after = Optional.empty();
        var again = (SaveResult.AlreadySaved) saving.save(ERIKA, previewOfErika);
        Letter saved = letter.saved.get(previewOfErika);
        assertThat(again.letter()).isEqualTo(saved);
        assertThat(documents.stored).containsOnlyKeys(saved.document());
    }

    @Test
    void ifErikaSavesWhileUpdatingThePreviewARepetitionFindsTheLetter() {
        // Given: Das Aktualisieren hat den offenen Snapshot gelesen; vor dem Löschen speichert eine andere Anfrage
        SaveResult[] concurrent = new SaveResult[1];
        snapshots.beforeDeletion = () -> concurrent[0] = saving.save(ERIKA, previewOfErika);

        // When: Das Aktualisieren will den bisherigen Snapshot löschen
        preview.update(ERIKA, CUSTOMER_4711, reminder.id(), previewOfErika);

        // Then: Der verbrauchte Snapshot bleibt; eine Wiederholung des Speicherns findet das Schreiben
        var saved = (SaveResult.Saved) concurrent[0];
        assertThat(snapshots.deleted).isEmpty();
        assertThat(saving.save(ERIKA, previewOfErika))
                .isEqualTo(new SaveResult.AlreadySaved(saved.letter()));
    }

    @Test
    void ifAConcurrentPreviewReplacesTheSnapshotNothingIsSaved() {
        // Given: Während Erika speichert, ersetzt sie die Vorschau in einem anderen Tab
        letter.before = () -> preview.update(ERIKA, CUSTOMER_4711, reminder.id(), previewOfErika);

        var result = saving.save(ERIKA, previewOfErika);

        // Then: kein Schreiben, kein verwaistes PDF; gespeichert wird aus der neuen Vorschau
        assertThat(result).isInstanceOf(SaveResult.SnapshotUnknown.class);
        assertThat(letter.saved).isEmpty();
        assertThat(documents.stored).isEmpty();
    }

    @Test
    void concurrentSavingOfTheSamePreviewYieldsOneLetter() {
        // Given: Eine zweite Anfrage verbraucht den Snapshot, während diese ihr PDF erzeugt
        LetterFromSnapshot ofTheSecondRequest = Letter.fromSnapshot(LetterId.generate(),
                snapshots.load(previewOfErika).orElseThrow(), new DocumentReference("store/zweite"),
                Checksum.of(new byte[] {1}), clock.now);
        letter.before = () -> letter.adopt(ofTheSecondRequest);

        var result = saving.save(ERIKA, previewOfErika);

        assertThat(result).isEqualTo(new SaveResult.AlreadySaved(ofTheSecondRequest.letter()));
        assertThat(letter.saved).containsOnly(Map.entry(previewOfErika, ofTheSecondRequest.letter()));
        assertThat(documents.stored).isEmpty();
    }

    @Test
    void aNewPreviewYieldsANewLetter() {
        var first = (SaveResult.Saved) saving.save(ERIKA, previewOfErika);
        SnapshotId newPreview = createPreview(ERIKA);

        var second = saving.save(ERIKA, newPreview);

        assertThat(second).isInstanceOf(SaveResult.Saved.class).isNotEqualTo(first);
        assertThat(letter.saved).hasSize(2);
    }

    @Test
    void withoutPermissionItStaysHiddenWhetherItWasAlreadySaved() {
        saving.save(ERIKA, previewOfErika);
        host.allowed = false;

        assertThat(saving.save(ERIKA, previewOfErika)).isInstanceOf(SaveResult.NotAllowed.class);
    }

    @ParameterizedTest(name = "{0} gelöscht")
    @ValueSource(strings = {"hard", "logical"})
    void ifTheLetterIsDeletedARepeatedSaveFindsNothing(String kind) {
        var saved = (SaveResult.Saved) saving.save(ERIKA, previewOfErika);
        delete(saved.letter().id(), kind);

        var again = saving.save(ERIKA, previewOfErika);

        assertThat(again).isInstanceOf(SaveResult.SnapshotUnknown.class);
        assertThat(renderer.pdfs).as("kein weiteres PDF").isEqualTo(1);
    }

    @ParameterizedTest(name = "{0} gelöscht")
    @ValueSource(strings = {"hard", "logical"})
    void ifTheLetterOfAConcurrentRequestIsDeletedNothingIsSaved(String kind) {
        // Given: Eine zweite Anfrage speichert dieselbe Vorschau, ihr Schreiben wird gelöscht, bevor diese speichert
        byte[] pdfOfTheSecond = {1};
        LetterFromSnapshot ofTheSecondRequest = Letter.fromSnapshot(LetterId.generate(),
                snapshots.load(previewOfErika).orElseThrow(), documents.put(pdfOfTheSecond),
                Checksum.of(pdfOfTheSecond), clock.now);
        letter.before = () -> {
            letter.adopt(ofTheSecondRequest);
            delete(ofTheSecondRequest.letter().id(), kind);
        };

        var result = saving.save(ERIKA, previewOfErika);

        // Then: kein Schreiben, und das eigene PDF ist wieder entfernt
        assertThat(result).isInstanceOf(SaveResult.SnapshotUnknown.class);
        assertThat(letter.saved.values()).noneMatch(Letter::isVisible);
        assertThat(documents.stored.keySet()).as("nur noch das PDF eines logisch gelöschten Schreibens")
                .isSubsetOf(ofTheSecondRequest.letter().document());
    }

    // Rückruf nach dem Speichern (rueckruf-nach-dem-speichern.feature)

    @Test
    void ifSavingCreatesALetterTheHostReceivesACallback() {
        var result = saving.save(ERIKA, previewOfErika);

        Letter saved = letter.saved.get(previewOfErika);
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
        callback.result = newOne -> CallbackResult.rejected("Die Aufgabe ist bereits erledigt");

        var result = saving.save(ERIKA, previewOfErika);

        assertThat(result).isEqualTo(new SaveResult.RejectedByHost("Die Aufgabe ist bereits erledigt"));
        assertThat(callback.invocations).hasSize(1);
    }

    @Test
    void anErrorInTheCallbackIsATechnicalError() {
        callback.result = newOne -> {
            throw new IllegalStateException("Aufgabenverwaltung nicht erreichbar");
        };

        assertThatThrownBy(() -> saving.save(ERIKA, previewOfErika))
                .isInstanceOf(IllegalStateException.class).hasMessage("Aufgabenverwaltung nicht erreichbar");
        assertThat(callback.invocations).as("nicht wiederholt").hasSize(1);
    }

    @Test
    void ifNothingIsCreatedThereIsNoCallback() {
        checker.result = pdf -> Conformance.notConformant(List.of("PDF/UA-1: Bild ohne Alternativtext"));
        saving.save(ERIKA, previewOfErika);
        host.allowed = false;
        saving.save(ERIKA, previewOfErika);

        assertThat(callback.invocations).isEmpty();
    }

    private void delete(LetterId id, String kind) {
        var policy = new Fakes.DeletionPolicy();
        policy.decision = kind.equals("hard") ? DeletionDecision.hard() : DeletionDecision.logical();
        host.allow(ERIKA, LetterAction.DELETE, CUSTOMER_4711);
        new DeleteLetterService(letter, host, policy, documents, clock).delete(ERIKA, id);
    }

    @Test
    void aMissingTemplateVersionIsCorruptData() {
        templates.letterheads.clear();

        assertThatThrownBy(() -> saving.save(ERIKA, previewOfErika))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("missing");
        templates.letterTemplate.clear();
        assertThatThrownBy(() -> saving.save(ERIKA, previewOfErika))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("missing");
    }

    private SnapshotId createPreview(UserId user) {
        host.allowed = true;
        return ((PreviewResult.Created) preview.create(user, CUSTOMER_4711, reminder.id())).snapshot();
    }

    /** Auch der Snapshot bleibt offen und behält seinen Datenkontext für einen weiteren Versuch. */
    private void nothingSavedOrStored() {
        assertThat(letter.saved).isEmpty();
        assertThat(documents.stored).isEmpty();
        assertThat(snapshots.load(previewOfErika).orElseThrow().state())
                .isInstanceOf(SnapshotState.Open.class);
    }
}
