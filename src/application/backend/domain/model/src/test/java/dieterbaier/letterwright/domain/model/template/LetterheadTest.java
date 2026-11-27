package dieterbaier.letterwright.domain.model.template;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.DataContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The rules across all versions of a template, checked on the letterhead. The letter template shares them. */
class LetterheadTest {

    private final TemplateId id = TemplateId.generate();
    private final Letterhead fresh = Letterhead.createWithDefaultType(id, "Business letter", "<p>Hello</p>");

    private static TemplateVersion version(TemplateId template, int number, TemplateVersionState state) {
        return TemplateVersion.restore(template, new VersionNumber(number), state, "<p>" + number + "</p>",
                DataContext.from(Map.of()));
    }

    @Test
    void aNewLetterheadHasTheFirstVersionAsDraft() {
        assertThat(fresh.type()).isEqualTo(LetterheadType.BUSINESS_FORM_B);
        assertThat(fresh.versions()).singleElement().satisfies(version -> {
            assertThat(version.number()).isEqualTo(VersionNumber.FIRST);
            assertThat(version.state()).isEqualTo(TemplateVersionState.DRAFT);
            assertThat(version.content()).isEqualTo("<p>Hello</p>");
            assertThat(version.reference()).isEqualTo(new VersionReference(id, VersionNumber.FIRST));
        });
        assertThat(fresh.draft()).isPresent();
        assertThat(fresh.publishedVersion()).isEmpty();
        assertThat(fresh.draft()).get().extracting(TemplateVersion::sampleData)
                .as("no sample data yet").isEqualTo(DataContext.from(Map.of()));
    }

    @Test
    void aDraftIsChangeableAndMayBeEmpty() {
        Letterhead changed = fresh.changeDraft("");

        assertThat(changed.draft()).get().extracting(TemplateVersion::content).isEqualTo("");
        assertThat(fresh.draft()).get().extracting(TemplateVersion::content)
                .as("the original instance stays unchanged").isEqualTo("<p>Hello</p>");
    }

    @Test
    void theDraftCarriesItsSampleDataAndANewDraftTakesThemOver() {
        DataContext sampleData = DataContext.from(Map.of("recipient", "Erika Mustermann"));

        Letterhead withData = fresh.changeDraft("<p>Hello</p>", sampleData);
        Letterhead contentOnly = withData.changeDraft("<p>Good day</p>");
        Letterhead next = contentOnly.publishDraft().newDraft("<p>new</p>");

        assertThat(contentOnly.draft()).get().extracting(TemplateVersion::sampleData).isEqualTo(sampleData);
        assertThat(next.draft()).get().extracting(TemplateVersion::sampleData).isEqualTo(sampleData);
        rejects(() -> fresh.changeDraft("<p>Hello</p>", null), "sample data of the version");
    }

    @Test
    void aMissingContentIsRejected() {
        rejects(() -> Letterhead.createWithDefaultType(id, "Business letter", null), "content of the version");
        rejects(() -> fresh.changeDraft(null), "content of the version");
        rejects(() -> fresh.publishDraft().newDraft(null), "content of the version");
    }

    @Test
    void anEmptyDraftCannotBePublished() {
        Letterhead empty = fresh.changeDraft("  ");

        assertThatThrownBy(empty::publishDraft)
                .isInstanceOf(IllegalStateTransition.class)
                .hasMessageContaining("without content");
    }

    @Test
    void lifecycleDraftPublishedWithdrawn() {
        Letterhead published = fresh.publishDraft();
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
        Letterhead withV2 = fresh.publishDraft().newDraft("<p>2</p>").publishDraft();

        assertThat(withV2.versions()).extracting(TemplateVersion::state)
                .containsExactly(TemplateVersionState.SUPERSEDED, TemplateVersionState.PUBLISHED);
        assertThat(withV2.publishedVersion()).get()
                .extracting(TemplateVersion::number).isEqualTo(new VersionNumber(2));
        assertThat(withV2.version(VersionNumber.FIRST)).get().extracting(TemplateVersion::content)
                .as("the superseded version stays unchanged").isEqualTo("<p>Hello</p>");
    }

    @Test
    void afterWithdrawalTheNextPublicationSupersedesNothing() {
        Letterhead withV2 = fresh.publishDraft().withdraw().newDraft("<p>2</p>")
                .publishDraft();

        assertThat(withV2.versions()).extracting(TemplateVersion::state)
                .containsExactly(TemplateVersionState.WITHDRAWN, TemplateVersionState.PUBLISHED);
    }

    @Test
    void aPublishedVersionIsImmutable() {
        Letterhead published = fresh.publishDraft();

        assertThatThrownBy(() -> published.changeDraft("<p>different</p>"))
                .isInstanceOf(IllegalStateTransition.class)
                .hasMessageContaining("Changing requires a draft")
                .hasMessageContaining("PUBLISHED");
        assertThatThrownBy(published::publishDraft).isInstanceOf(IllegalStateTransition.class);
    }

    @Test
    void aChangeAfterPublishingIsTheNextDraft() {
        Letterhead withV2 = fresh.publishDraft().newDraft("<p>new</p>");

        assertThat(withV2.versions()).extracting(TemplateVersion::number)
                .containsExactly(VersionNumber.FIRST, new VersionNumber(2));
        assertThat(withV2.draft()).get().extracting(TemplateVersion::number).isEqualTo(new VersionNumber(2));
        assertThat(withV2.publishedVersion()).get()
                .extracting(TemplateVersion::number).isEqualTo(VersionNumber.FIRST);
    }

    @Test
    void thereIsAtMostOneDraft() {
        assertThatThrownBy(() -> fresh.newDraft("<p>second</p>"))
                .isInstanceOf(IllegalStateTransition.class)
                .hasMessageContaining("at most one");
    }

    @Test
    void newNumbersFollowTheHighestEvenAfterWithdrawal() {
        Letterhead withV2 = fresh.publishDraft().withdraw().newDraft("<p>2</p>");

        assertThat(withV2.draft()).get().extracting(TemplateVersion::number).isEqualTo(new VersionNumber(2));
    }

    @Test
    void withdrawNeedsAPublishedVersion() {
        assertThatThrownBy(fresh::withdraw)
                .isInstanceOf(IllegalStateTransition.class)
                .hasMessageContaining("requires a published version");
        Letterhead withdrawn = fresh.publishDraft().withdraw();
        assertThatThrownBy(withdrawn::withdraw).isInstanceOf(IllegalStateTransition.class);
    }

    @Test
    void restoreSortsByNumber() {
        Letterhead loaded = Letterhead.restore(id, "Business letter", LetterheadType.PRIVATE, List.of(
                version(id, 2, TemplateVersionState.DRAFT),
                version(id, 1, TemplateVersionState.PUBLISHED)));

        assertThat(loaded.versions()).extracting(version -> version.number().value()).containsExactly(1, 2);
        assertThat(loaded.type()).isEqualTo(LetterheadType.PRIVATE);
    }

    @Test
    void restoreChecksTheRulesAcrossAllVersions() {
        TemplateId foreign = TemplateId.generate();
        LetterheadType type = LetterheadType.PRIVATE;

        rejects(() -> Letterhead.restore(id, "B", type, List.of()), "at least one version");
        rejects(() -> Letterhead.restore(id, "B", type, null), "The versions");
        rejects(() -> Letterhead.restore(id, "B", type,
                Arrays.asList(version(id, 1, TemplateVersionState.DRAFT), null)), "The versions[1]");
        rejects(() -> Letterhead.restore(id, "B", type,
                List.of(version(foreign, 1, TemplateVersionState.DRAFT))), "belongs to template");
        rejects(() -> Letterhead.restore(id, "B", type,
                List.of(version(id, 2, TemplateVersionState.DRAFT))), "expected 1, found 2");
        rejects(() -> Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.WITHDRAWN), version(id, 1, TemplateVersionState.WITHDRAWN))),
                "expected 2, found 1");
        rejects(() -> Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.DRAFT), version(id, 2, TemplateVersionState.PUBLISHED))),
                "not the newest");
    }

    @Test
    void restoreLeavesAtMostTheNewestReleasedOnePublished() {
        LetterheadType type = LetterheadType.PRIVATE;

        rejects(() -> Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.PUBLISHED), version(id, 2, TemplateVersionState.PUBLISHED))),
                "Version 1 is published");
        rejects(() -> Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.PUBLISHED), version(id, 2, TemplateVersionState.WITHDRAWN))),
                "at most one version per template is published");
        rejects(() -> Letterhead.restore(id, "B", type, List.of(version(id, 1, TemplateVersionState.SUPERSEDED))),
                "no newer version is released");
        rejects(() -> Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.SUPERSEDED), version(id, 2, TemplateVersionState.DRAFT))),
                "Version 1 is superseded");

        Letterhead loaded = Letterhead.restore(id, "B", type, List.of(
                version(id, 1, TemplateVersionState.WITHDRAWN), version(id, 2, TemplateVersionState.SUPERSEDED),
                version(id, 3, TemplateVersionState.PUBLISHED), version(id, 4, TemplateVersionState.DRAFT)));
        assertThat(loaded.publishedVersion()).get()
                .extracting(TemplateVersion::number).isEqualTo(new VersionNumber(3));
    }

    @Test
    void requiredAttributesOfTheLetterhead() {
        rejects(() -> Letterhead.create(null, "B", LetterheadType.PRIVATE, ""), "ID of the letterhead");
        rejects(() -> Letterhead.create(id, null, LetterheadType.PRIVATE, ""), "name of the letterhead");
        rejects(() -> Letterhead.create(id, " ", LetterheadType.PRIVATE, ""), "name of the letterhead");
        rejects(() -> Letterhead.create(id, "B", null, ""), "type of the letterhead");
        rejects(() -> Letterhead.restore(null, "B", LetterheadType.PRIVATE, List.of()),
                "ID of the letterhead");
    }

    @Test
    void equalityOfAggregatesByIdentity() {
        Letterhead otherState = fresh.changeDraft("<p>different</p>").publishDraft();
        Letterhead sameContentOtherId = Letterhead.createWithDefaultType(TemplateId.generate(), "Business letter",
                "<p>Hello</p>");

        assertThat(otherState).isEqualTo(fresh).hasSameHashCodeAs(fresh);
        assertThat(sameContentOtherId).isNotEqualTo(fresh);
        assertThat(fresh).isNotEqualTo("Business letter");
        assertThat(fresh.toString()).contains(id.value().toString(), "Business letter");
    }
}
