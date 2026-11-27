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
 * Brücke zu {@code features/letter/preview-create.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die ausgehenden Ports sind {@link Fakes}, die festhalten, was gefragt wurde.
 */
class CreatePreviewServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-22T10:00:00Z");
    private static final UserId ERIKA = new UserId("erika");
    private static final UserId MAX = new UserId("max");
    private static final BusinessReference CUSTOMER_4711 = new BusinessReference("Kunde", "4711");

    private final Fakes.Templates templates = new Fakes.Templates();
    private final Fakes.Host host = new Fakes.Host();
    private final Fakes.Snapshots store = new Fakes.Snapshots();
    private final Fakes.StoppedClock clock = new Fakes.StoppedClock(NOW);
    private final Fakes.Renderer renderer = new Fakes.Renderer();
    private final CreatePreviewService preview = new CreatePreviewService(templates, host, host, renderer, store, clock);

    // Grundlage: "Mahnung" zum Bezugstyp Kunde, Version 1 abgelöst, veröffentlicht ist Version 2, Entwurf 3; Briefpapier in Version 1;
    // der Host liefert zum Kunden 4711 den Datenkontext "name" = "Erika Mustermann GmbH".
    private final Letterhead letterhead =
            templates.store(Letterhead.createWithDefaultType(TemplateId.generate(), "Standard", "<header/>")
                    .publishDraft());
    private final LetterTemplate reminder = templates.store(LetterTemplate.create(TemplateId.generate(), "Mahnung",
            letterhead.id(), "Kunde", "<p>{{name}}</p>").publishDraft()
            .newDraft("<p>Erneut: {{name}}</p>").publishDraft().newDraft("<p>Entwurf</p>"));

    @Test
    void withPermissionAPreviewWithSnapshotIsCreated() {
        // Given: Erika darf zum Kunden 4711 aus der Mahnung ein Schreiben erzeugen
        host.allowed = true;

        // When: Erika erzeugt die Vorschau der Mahnung zum Kunden 4711
        var result = preview.create(ERIKA, CUSTOMER_4711, reminder.id());

        // Then: Vorschau mit den Daten; Snapshot gebunden an Erika, Kunde, Briefpapier V1 und Mahnung V2; 30 Minuten
        var created = (PreviewResult.Created) result;
        assertThat(created.html()).contains("Erneut: Erika Mustermann GmbH").contains("<header/>");
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
        // Given: Erika darf aus der Mahnung kein Schreiben erzeugen
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

        var result = preview.create(ERIKA, new BusinessReference("Lieferant", "0815"), reminder.id());

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
    @ValueSource(strings = {"das Standardschreiben", "das Briefpapier"})
    void withoutPublishedVersionThereIsNoPreview(String template) {
        // Given: <vorlage> hat keine veröffentlichte Version; Erika darf erzeugen
        withoutPublishedVersion(template);
        host.allowed = true;

        var result = preview.create(ERIKA, CUSTOMER_4711, reminder.id());

        assertThat(result).isInstanceOf(PreviewResult.NoPublishedVersion.class);
        assertThat(host.creationRequested).containsExactly(reminder);
        assertThat(host.dataContextRequested).isZero();
        assertThat(store.snapshots).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"das Standardschreiben", "das Briefpapier"})
    void withoutPermissionItStaysHiddenWhetherAVersionIsPublished(String template) {
        // Given: <vorlage> hat keine veröffentlichte Version; Erika darf nicht erzeugen
        withoutPublishedVersion(template);
        host.allowed = false;

        var result = preview.create(ERIKA, CUSTOMER_4711, reminder.id());

        // Then: nicht erlaubt, wie mit veröffentlichter Version; kein Datenkontext, kein Rendering, kein Snapshot
        assertThat(result).isInstanceOf(PreviewResult.NotAllowed.class);
        assertThat(host.dataContextRequested).isZero();
        assertThat(renderer.invocations).isZero();
        assertThat(store.snapshots).isEmpty();
    }

    private void withoutPublishedVersion(String template) {
        if (template.equals("das Standardschreiben")) {
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
        // Given: Erika darf erzeugen und hat vor 20 Minuten eine Vorschau erzeugt
        host.allowed = true;
        var previous = (PreviewResult.Created) preview.create(ERIKA, CUSTOMER_4711, reminder.id());
        clock.now = NOW.plus(Duration.ofMinutes(20));

        // When: Erika aktualisiert diese Vorschau
        var newOnes = (PreviewResult.Created) preview.update(ERIKA, CUSTOMER_4711, reminder.id(),
                previous.snapshot());

        // Then: Der bisherige Snapshot ist gelöscht, der neue läuft in 30 Minuten ab
        assertThat(store.snapshots).containsOnlyKeys(newOnes.snapshot());
        assertThat(newOnes.expiry()).isEqualTo(clock.now.plus(Duration.ofMinutes(30)));
    }

    @Test
    void aForeignSnapshotBehavesLikeAMissingOne() {
        // Given: Erika darf erzeugen; Max hat eine Vorschau der Mahnung zum Kunden 4711
        host.allowed = true;
        var ofMax = (PreviewResult.Created) preview.create(MAX, CUSTOMER_4711, reminder.id());
        Snapshot snapshotOfMax = store.snapshots.get(ofMax.snapshot());

        // When: Erika aktualisiert und gibt dabei den Snapshot von Max an
        var result = preview.update(ERIKA, CUSTOMER_4711, reminder.id(), ofMax.snapshot());

        // Then: Für Erika entsteht eine neue Vorschau; der Snapshot von Max ist unverändert gespeichert
        var forErika = (PreviewResult.Created) result;
        assertThat(store.snapshots.get(forErika.snapshot()).user()).isEqualTo(ERIKA);
        assertThat(store.snapshots.get(ofMax.snapshot())).isSameAs(snapshotOfMax);
        assertThat(store.deleted).isEmpty();
    }

    @Test
    void anUpdatedPreviewLeavesAConsumedSnapshotInPlace() {
        // Given: Erika hat aus ihrer bisherigen Vorschau schon ein Schreiben gespeichert
        host.allowed = true;
        var previous = (PreviewResult.Created) preview.create(ERIKA, CUSTOMER_4711, reminder.id());
        store.save(store.snapshots.get(previous.snapshot()).consumed());

        // When: Erika aktualisiert diese Vorschau
        var newOnes = (PreviewResult.Created) preview.update(ERIKA, CUSTOMER_4711, reminder.id(),
                previous.snapshot());

        // Then: Der verbrauchte Snapshot bleibt, damit eine Wiederholung des Speicherns das Schreiben noch findet
        assertThat(store.snapshots).containsOnlyKeys(previous.snapshot(), newOnes.snapshot());
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
