package dieterbaier.letterwright.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotState;
import dieterbaier.letterwright.domain.model.template.Letterhead;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import dieterbaier.letterwright.usecases.in.PreviewResult;
import dieterbaier.letterwright.usecases.out.Renderer;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Bridge to {@code features/letters/create-preview.feature}: one test per scenario, named after the
 * scenario title. The outbound ports are {@link Fakes} that record what was asked.
 */
class CreatePreviewServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-22T10:00:00Z");
    private static final UserId ERIKA = new UserId("erika");
    private static final UserId MAX = new UserId("max");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Customer", "4711");

    private final Fakes.Templates templates = new Fakes.Templates();
    private final Fakes.Host host = new Fakes.Host();
    private final Fakes.Snapshots store = new Fakes.Snapshots();
    private final Fakes.StoppedClock clock = new Fakes.StoppedClock(NOW);
    private final Fakes.Renderer renderer = new Fakes.Renderer();
    private final CreatePreviewService preview = new CreatePreviewService(templates, host, host, renderer, store, clock);

    // Background: "Payment reminder" for reference type Customer, version 1 superseded, version 2 published, draft 3;
    // letterhead in version 1; for customer 4711 the host provides the data context "name" = "Erika Mustermann GmbH".
    private final Letterhead letterhead =
            templates.store(Letterhead.createWithDefaultType(TemplateId.generate(), "Standard", "<header/>")
                    .publishDraft());
    private final LetterTemplate reminder = templates.store(LetterTemplate.create(TemplateId.generate(), "Payment reminder",
            letterhead.id(), "Customer", "<p>{{name}}</p>").publishDraft()
            .newDraft("<p>Again: {{name}}</p>").publishDraft().newDraft("<p>Draft</p>"));

    @Test
    void withPermissionAPreviewWithSnapshotIsCreated() {
        // Given: Erika may create a letter from the payment reminder for customer 4711
        host.allowed = true;

        // When: Erika creates the preview of the payment reminder for customer 4711
        var result = preview.create(ERIKA, CUSTOMER_4711, reminder.id());

        // Then: preview with the data; snapshot bound to Erika, customer, letterhead V1 and payment reminder V2; 30 minutes
        var created = (PreviewResult.Created) result;
        assertThat(created.html()).contains("Again: Erika Mustermann GmbH").contains("<header/>");
        Snapshot snapshot = store.snapshots.get(created.snapshot());
        assertThat(snapshot.user()).isEqualTo(ERIKA);
        assertThat(snapshot.reference()).isEqualTo(CUSTOMER_4711);
        assertThat(snapshot.letterhead()).isEqualTo(new VersionReference(letterhead.id(), VersionNumber.FIRST));
        assertThat(snapshot.letterTemplate()).isEqualTo(new VersionReference(reminder.id(), new VersionNumber(2)));
        assertThat(snapshot.state()).isEqualTo(new SnapshotState.Open(host.dataContext));
        assertThat(created.expiry()).isEqualTo(snapshot.expiry()).isEqualTo(NOW.plus(Duration.ofMinutes(30)));
        assertThat(host.creationRequested).containsExactly(reminder);
    }

    @Test
    void withoutPermissionNoDataContextIsRequested() {
        // Given: Erika may not create a letter from the payment reminder
        host.allowed = false;

        // When
        var result = preview.create(ERIKA, CUSTOMER_4711, reminder.id());

        // Then
        assertThat(result).isInstanceOf(PreviewResult.NotAllowed.class);
        assertThat(host.creationRequested).containsExactly(reminder);
        assertThat(host.dataContextRequested).isZero();
        assertThat(renderer.invocations).isZero();
        assertThat(store.snapshots).isEmpty();
    }

    @Test
    void forAReferenceOfAnotherTypeTheHostIsNotAsked() {
        host.allowed = true;

        var result = preview.create(ERIKA, new BusinessReference("Supplier", "0815"), reminder.id());

        assertThat(result).isInstanceOf(PreviewResult.ReferenceMismatch.class);
        assertThat(host.wasAsked()).isFalse();
        assertThat(store.snapshots).isEmpty();
    }

    @Test
    void forAnUnknownLetterTemplateThereIsNoPreview() {
        host.allowed = true;

        var result = preview.create(ERIKA, CUSTOMER_4711, TemplateId.generate());

        assertThat(result).isInstanceOf(PreviewResult.LetterTemplateUnknown.class);
        assertThat(host.wasAsked()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"the letter template", "the letterhead"})
    void withoutPublishedVersionThereIsNoPreview(String template) {
        // Given: <template> has no published version; Erika may create
        withoutPublishedVersion(template);
        host.allowed = true;

        var result = preview.create(ERIKA, CUSTOMER_4711, reminder.id());

        assertThat(result).isInstanceOf(PreviewResult.NoPublishedVersion.class);
        assertThat(host.creationRequested).containsExactly(reminder);
        assertThat(host.dataContextRequested).isZero();
        assertThat(store.snapshots).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"the letter template", "the letterhead"})
    void withoutPermissionItStaysHiddenWhetherAVersionIsPublished(String template) {
        // Given: <template> has no published version; Erika may not create
        withoutPublishedVersion(template);
        host.allowed = false;

        var result = preview.create(ERIKA, CUSTOMER_4711, reminder.id());

        // Then: not allowed, as with a published version; no data context, no rendering, no snapshot
        assertThat(result).isInstanceOf(PreviewResult.NotAllowed.class);
        assertThat(host.dataContextRequested).isZero();
        assertThat(renderer.invocations).isZero();
        assertThat(store.snapshots).isEmpty();
    }

    private void withoutPublishedVersion(String template) {
        if (template.equals("the letter template")) {
            templates.store(reminder.withdraw());
        } else {
            templates.store(letterhead.withdraw());
        }
    }

    @Test
    void aMissingLetterheadCountsAsOneWithoutPublishedVersion() {
        templates.letterheads.clear();
        host.allowed = true;

        assertThat(preview.create(ERIKA, CUSTOMER_4711, reminder.id()))
                .isInstanceOf(PreviewResult.NoPublishedVersion.class);
    }

    @Test
    void anUpdatedPreviewReplacesTheOwnSnapshot() {
        // Given: Erika may create and created a preview 20 minutes ago
        host.allowed = true;
        var previous = (PreviewResult.Created) preview.create(ERIKA, CUSTOMER_4711, reminder.id());
        clock.now = NOW.plus(Duration.ofMinutes(20));

        // When: Erika updates this preview
        var newOne = (PreviewResult.Created) preview.update(ERIKA, CUSTOMER_4711, reminder.id(),
                previous.snapshot());

        // Then: the previous snapshot is deleted, the new one expires in 30 minutes
        assertThat(store.snapshots).containsOnlyKeys(newOne.snapshot());
        assertThat(newOne.expiry()).isEqualTo(clock.now.plus(Duration.ofMinutes(30)));
    }

    @Test
    void aForeignSnapshotBehavesLikeAMissingOne() {
        // Given: Erika may create; Max has a preview of the payment reminder for customer 4711
        host.allowed = true;
        var ofMax = (PreviewResult.Created) preview.create(MAX, CUSTOMER_4711, reminder.id());
        Snapshot snapshotOfMax = store.snapshots.get(ofMax.snapshot());

        // When: Erika updates, passing the snapshot of Max
        var result = preview.update(ERIKA, CUSTOMER_4711, reminder.id(), ofMax.snapshot());

        // Then: a new preview is created for Erika; the snapshot of Max is stored unchanged
        var forErika = (PreviewResult.Created) result;
        assertThat(store.snapshots.get(forErika.snapshot()).user()).isEqualTo(ERIKA);
        assertThat(store.snapshots.get(ofMax.snapshot())).isSameAs(snapshotOfMax);
        assertThat(store.deleted).isEmpty();
    }

    @Test
    void anUpdatedPreviewLeavesAConsumedSnapshotInPlace() {
        // Given: Erika has already saved a letter from her previous preview
        host.allowed = true;
        var previous = (PreviewResult.Created) preview.create(ERIKA, CUSTOMER_4711, reminder.id());
        store.save(store.snapshots.get(previous.snapshot()).consumed());

        // When: Erika updates this preview
        var newOne = (PreviewResult.Created) preview.update(ERIKA, CUSTOMER_4711, reminder.id(),
                previous.snapshot());

        // Then: the consumed snapshot stays so that a retry of the save still finds the letter
        assertThat(store.snapshots).containsOnlyKeys(previous.snapshot(), newOne.snapshot());
        assertThat(store.deleted).isEmpty();
    }

    @Test
    void anUnknownPreviousSnapshotIsIgnored() {
        host.allowed = true;

        var result = preview.update(ERIKA, CUSTOMER_4711, reminder.id(), SnapshotId.generate());

        assertThat(result).isInstanceOf(PreviewResult.Created.class);
        assertThat(store.deleted).isEmpty();
    }

    @Test
    void withoutNewPreviewThePreviousSnapshotRemains() {
        host.allowed = true;
        var previous = (PreviewResult.Created) preview.create(ERIKA, CUSTOMER_4711, reminder.id());
        host.allowed = false;

        var result = preview.update(ERIKA, CUSTOMER_4711, reminder.id(), previous.snapshot());

        assertThat(result).isInstanceOf(PreviewResult.NotAllowed.class);
        assertThat(store.snapshots).containsOnlyKeys(previous.snapshot());
    }
}
