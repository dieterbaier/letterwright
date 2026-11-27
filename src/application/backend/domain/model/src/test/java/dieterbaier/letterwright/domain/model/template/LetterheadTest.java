package dieterbaier.letterwright.domain.model.template;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.DataContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Die Regeln über alle Versionen einer Vorlage, geprüft am Briefpapier. Das Standardschreiben teilt sie. */
class LetterheadTest {

    private final TemplateId id = TemplateId.generate();
    private final Letterhead generate = Letterhead.createWithDefaultType(id, "Geschäftsbrief", "<p>Hallo</p>");

    private static TemplateVersion version(TemplateId template, int number, TemplateVersionState state) {
        return TemplateVersion.restore(template, new VersionNumber(number), state, "<p>" + number + "</p>",
                DataContext.from(Map.of()));
    }

    @Test
    void aNewLetterheadHasTheFirstVersionAsDraft() {
        assertThat(generate.type()).isEqualTo(LetterheadType.BUSINESS_FORM_B);
        assertThat(generate.versions()).singleElement().satisfies(version -> {
            assertThat(version.number()).isEqualTo(VersionNumber.FIRST);
            assertThat(version.state()).isEqualTo(TemplateVersionState.DRAFT);
            assertThat(version.content()).isEqualTo("<p>Hallo</p>");
            assertThat(version.reference()).isEqualTo(new VersionReference(id, VersionNumber.FIRST));
        });
        assertThat(generate.draft()).isPresent();
        assertThat(generate.publishedVersion()).isEmpty();
        assertThat(generate.draft()).get().extracting(TemplateVersion::sampleData)
                .as("noch ohne Beispieldaten").isEqualTo(DataContext.from(Map.of()));
    }

    @Test
    void aDraftIsChangeableAndMayBeEmpty() {
        Letterhead changed = generate.changeDraft("");

        assertThat(changed.draft()).get().extracting(TemplateVersion::content).isEqualTo("");
        assertThat(generate.draft()).get().extracting(TemplateVersion::content)
                .as("die Ausgangsinstanz bleibt unverändert").isEqualTo("<p>Hallo</p>");
    }

    @Test
    void theDraftCarriesItsSampleDataAndANewDraftTakesThemOver() {
        DataContext sampleData = DataContext.from(Map.of("empfaenger", "Erika Mustermann"));

        Letterhead withData = generate.changeDraft("<p>Hallo</p>", sampleData);
        Letterhead contentOnly = withData.changeDraft("<p>Guten Tag</p>");
        Letterhead next = contentOnly.publishDraft().newDraft("<p>neu</p>");

        assertThat(contentOnly.draft()).get().extracting(TemplateVersion::sampleData).isEqualTo(sampleData);
        assertThat(next.draft()).get().extracting(TemplateVersion::sampleData).isEqualTo(sampleData);
        rejects(() -> generate.changeDraft("<p>Hallo</p>", null), "Beispieldaten der Version");
    }

    @Test
    void aMissingContentIsRejected() {
        rejects(() -> Letterhead.createWithDefaultType(id, "Geschäftsbrief", null), "Inhalt der Version");
        rejects(() -> generate.changeDraft(null), "Inhalt der Version");
        rejects(() -> generate.publishDraft().newDraft(null), "Inhalt der Version");
    }

    @Test
    void anEmptyDraftCannotBePublished() {
        Letterhead empty = generate.changeDraft("  ");

        assertThatThrownBy(empty::publishDraft)
                .isInstanceOf(IllegalStateTransition.class)
                .hasMessageContaining("ohne Inhalt");
    }

    @Test
    void lifecycleDraftPublishedWithdrawn() {
        Letterhead published = generate.publishDraft();
        Letterhead withdrawn = published.withdraw();

        assertThat(published.draft()).isEmpty();
        assertThat(published.publishedVersion()).get()
                .extracting(TemplateVersion::number).isEqualTo(VersionNumber.FIRST);
        assertThat(withdrawn.version(VersionNumber.FIRST)).get()
                .extracting(TemplateVersion::state).isEqualTo(TemplateVersionState.WITHDRAWN);
        assertThat(withdrawn.publishedVersion()).isEmpty();
    }

    @Test
    void aNewlyPublishedVersionSupersedesThePreviousOne() {
        Letterhead withV2 = generate.publishDraft().newDraft("<p>2</p>").publishDraft();

        assertThat(withV2.versions()).extracting(TemplateVersion::state)
                .containsExactly(TemplateVersionState.SUPERSEDED, TemplateVersionState.PUBLISHED);
        assertThat(withV2.publishedVersion()).get()
                .extracting(TemplateVersion::number).isEqualTo(new VersionNumber(2));
        assertThat(withV2.version(VersionNumber.FIRST)).get().extracting(TemplateVersion::content)
                .as("die abgelöste Version bleibt unverändert").isEqualTo("<p>Hallo</p>");
    }

    @Test
    void afterWithdrawalTheNextPublicationSupersedesNothing() {
        Letterhead withV2 = generate.publishDraft().withdraw().newDraft("<p>2</p>")
                .publishDraft();

        assertThat(withV2.versions()).extracting(TemplateVersion::state)
                .containsExactly(TemplateVersionState.WITHDRAWN, TemplateVersionState.PUBLISHED);
    }

    @Test
    void aPublishedVersionIsImmutable() {
        Letterhead published = generate.publishDraft();

        assertThatThrownBy(() -> published.changeDraft("<p>anders</p>"))
                .isInstanceOf(IllegalStateTransition.class)
                .hasMessageContaining("Ändern braucht einen Entwurf")
                .hasMessageContaining("PUBLISHED");
        assertThatThrownBy(published::publishDraft).isInstanceOf(IllegalStateTransition.class);
    }

    @Test
    void aChangeAfterPublishingIsTheNextDraft() {
        Letterhead withV2 = generate.publishDraft().newDraft("<p>neu</p>");

        assertThat(withV2.versions()).extracting(TemplateVersion::number)
                .containsExactly(VersionNumber.FIRST, new VersionNumber(2));
        assertThat(withV2.draft()).get().extracting(TemplateVersion::number).isEqualTo(new VersionNumber(2));
        assertThat(withV2.publishedVersion()).get()
                .extracting(TemplateVersion::number).isEqualTo(VersionNumber.FIRST);
    }

    @Test
    void thereIsAtMostOneDraft() {
        assertThatThrownBy(() -> generate.newDraft("<p>zweiter</p>"))
                .isInstanceOf(IllegalStateTransition.class)
                .hasMessageContaining("höchstens einen");
    }

    @Test
    void newNumbersFollowTheHighestEvenAfterWithdrawal() {
        Letterhead withV2 = generate.publishDraft().withdraw().newDraft("<p>2</p>");

        assertThat(withV2.draft()).get().extracting(TemplateVersion::number).isEqualTo(new VersionNumber(2));
    }

    @Test
    void withdrawNeedsAPublishedVersion() {
        assertThatThrownBy(generate::withdraw)
                .isInstanceOf(IllegalStateTransition.class)
                .hasMessageContaining("braucht eine veröffentlichte Version");
        Letterhead withdrawn = generate.publishDraft().withdraw();
        assertThatThrownBy(withdrawn::withdraw).isInstanceOf(IllegalStateTransition.class);
    }

    @Test
    void restoreSortsByNumber() {
        Letterhead loaded = Letterhead.restore(id, "Geschäftsbrief", LetterheadType.PRIVATE, List.of(
                version(id, 2, TemplateVersionState.DRAFT),
                version(id, 1, TemplateVersionState.PUBLISHED)));

        assertThat(loaded.versions()).extracting(version -> version.number().value()).containsExactly(1, 2);
        assertThat(loaded.type()).isEqualTo(LetterheadType.PRIVATE);
    }

    @Test
    void restoreChecksTheRulesAcrossAllVersions() {
        TemplateId foreign = TemplateId.generate();
        LetterheadType type = LetterheadType.PRIVATE;

        rejects(() -> Letterhead.restore(id, "B", type, List.of()), "mindestens eine Version");
        rejects(() -> Letterhead.restore(id, "B", type, null), "Die Versionen");
        rejects(() -> Letterhead.restore(id, "B", type,
                Arrays.asList(version(id, 1, TemplateVersionState.DRAFT), null)), "Die Versionen[1]");
        rejects(() -> Letterhead.restore(id, "B", type,
                List.of(version(foreign, 1, TemplateVersionState.DRAFT))), "gehört zur Vorlage");
        rejects(() -> Letterhead.restore(id, "B", type,
                List.of(version(id, 2, TemplateVersionState.DRAFT))), "erwartet 1, gefunden 2");
        rejects(() -> Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.WITHDRAWN), version(id, 1, TemplateVersionState.WITHDRAWN))),
                "erwartet 2, gefunden 1");
        rejects(() -> Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.DRAFT), version(id, 2, TemplateVersionState.PUBLISHED))),
                "nicht die neueste");
    }

    @Test
    void restoreLeavesAtMostTheNewestReleasedOnePublished() {
        LetterheadType type = LetterheadType.PRIVATE;

        rejects(() -> Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.PUBLISHED), version(id, 2, TemplateVersionState.PUBLISHED))),
                "Version 1 ist veröffentlicht");
        rejects(() -> Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.PUBLISHED), version(id, 2, TemplateVersionState.WITHDRAWN))),
                "höchstens eine veröffentlicht");
        rejects(() -> Letterhead.restore(id, "B", type, List.of(version(id, 1, TemplateVersionState.SUPERSEDED))),
                "keine neuere Version ist freigegeben");
        rejects(() -> Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.SUPERSEDED), version(id, 2, TemplateVersionState.DRAFT))),
                "Version 1 ist abgelöst");

        Letterhead loaded = Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.WITHDRAWN), version(id, 2, TemplateVersionState.SUPERSEDED),
                version(id, 3, TemplateVersionState.PUBLISHED), version(id, 4, TemplateVersionState.DRAFT)));
        assertThat(loaded.publishedVersion()).get()
                .extracting(TemplateVersion::number).isEqualTo(new VersionNumber(3));
    }

    @Test
    void requiredAttributesOfTheLetterhead() {
        rejects(() -> Letterhead.create(null, "B", LetterheadType.PRIVATE, ""), "ID des Briefpapiers");
        rejects(() -> Letterhead.create(id, null, LetterheadType.PRIVATE, ""), "Name des Briefpapiers");
        rejects(() -> Letterhead.create(id, " ", LetterheadType.PRIVATE, ""), "Name des Briefpapiers");
        rejects(() -> Letterhead.create(id, "B", null, ""), "Typ des Briefpapiers");
        rejects(() -> Letterhead.restore(null, "B", LetterheadType.PRIVATE, List.of()),
                "ID des Briefpapiers");
    }

    @Test
    void equalityOfAggregatesByIdentity() {
        Letterhead otherState = generate.changeDraft("<p>anders</p>").publishDraft();
        Letterhead sameContentOtherId = Letterhead.createWithDefaultType(TemplateId.generate(), "Geschäftsbrief",
                "<p>Hallo</p>");

        assertThat(otherState).isEqualTo(generate).hasSameHashCodeAs(generate);
        assertThat(sameContentOtherId).isNotEqualTo(generate);
        assertThat(generate).isNotEqualTo("Geschäftsbrief");
        assertThat(generate.toString()).contains(id.value().toString(), "Geschäftsbrief");
    }
}
