package dieterbaier.letterwright.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.TemplateAction;
import dieterbaier.letterwright.domain.model.template.Letterhead;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.IllegalStateTransition;
import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import dieterbaier.letterwright.domain.model.template.TemplateVersion;
import dieterbaier.letterwright.domain.model.template.TemplateVersionState;
import dieterbaier.letterwright.usecases.in.PublicationResult;
import dieterbaier.letterwright.usecases.out.Conformance;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Brücke zu {@code features/templates/vorlagenversion-veroeffentlichen.feature}: ein Test je Szenario, benannt nach dem
 * Szenariotitel. Die ausgehenden Ports sind {@link Fakes}.
 */
class PublishTemplateVersionServiceTest {

    private static final UserId VERA = new UserId("vera");
    private static final UserId MAX = new UserId("max");
    private static final VersionNumber TWO = new VersionNumber(2);

    private final Fakes.Templates templates = new Fakes.Templates();
    private final Fakes.Host host = new Fakes.Host();
    private final Fakes.Renderer renderer = new Fakes.Renderer();
    private final Fakes.Checker checker = new Fakes.Checker();
    private final PublishTemplateVersionService publication =
            new PublishTemplateVersionService(templates, host, renderer, checker);

    // Grundlage: Briefpapier "Standard" V1 veröffentlicht mit Beispieldaten; "Mahnung" V1 veröffentlicht, Entwurf V2
    // mit eigenen Beispieldaten; Vera darf veröffentlichen.
    private final Letterhead letterhead = templates.store(Letterhead.createWithDefaultType(TemplateId.generate(),
            "Standard", "<header/>").changeDraft("<header/>",
                    DataContext.from(Map.of("absender", "Muster GmbH", "betreff", "Allgemein")))
            .publishDraft());
    private final LetterTemplate reminder = templates.store(LetterTemplate.create(TemplateId.generate(), "Mahnung",
            letterhead.id(), "Kunde", "<p>{{name}}</p>").publishDraft()
            .newDraft("<p>Erneut: {{name}}</p>")
            .changeDraft("<p>Erneut: {{name}}</p>",
                    DataContext.from(Map.of("name", "Erika Mustermann GmbH", "betreff", "Mahnung"))));

    {
        host.mayPublish.add(VERA);
    }

    @Test
    void aConformantDraftIsPublishedAndSupersedesThePreviousVersion() {
        var result = publication.publish(VERA, reminder.id());

        assertThat(result).isEqualTo(
                new PublicationResult.Published(new VersionReference(reminder.id(), TWO)));
        LetterTemplate saved = savedReminder();
        assertThat(saved.versions()).extracting(TemplateVersion::state)
                .containsExactly(TemplateVersionState.SUPERSEDED, TemplateVersionState.PUBLISHED);
        assertThat(saved.version(VersionNumber.FIRST)).get().extracting(TemplateVersion::content)
                .isEqualTo("<p>{{name}}</p>");
        assertThat(host.templateActionRequested).containsExactly(TemplateAction.PUBLISH);
    }

    @Test
    void theProofPdfIsCreatedWithThePublishedLetterheadAndTheSampleDataOfBothVersions() {
        List<String> checked = new ArrayList<>();
        checker.result = pdf -> {
            checked.add(new String(pdf, StandardCharsets.UTF_8));
            return Conformance.conformant();
        };

        publication.publish(VERA, reminder.id());

        assertThat(renderer.pdfData).containsExactly(DataContext.from(Map.of(
                "absender", "Muster GmbH", "betreff", "Mahnung", "name", "Erika Mustermann GmbH")));
        assertThat(checked).containsExactly("<header/><p>Erneut: Erika Mustermann GmbH</p>");
    }

    @Test
    void aLetterheadIsCheckedWithTheBuiltInSampleContentAndPublished() {
        templates.store(letterhead.newDraft("<header>neu</header>"));
        List<String> checked = new ArrayList<>();
        checker.result = pdf -> {
            checked.add(new String(pdf, StandardCharsets.UTF_8));
            return Conformance.conformant();
        };

        var result = publication.publish(VERA, letterhead.id());

        assertThat(checked).containsExactly("<header>neu</header>Beispielinhalt");
        assertThat(renderer.pdfData).containsExactly(letterhead.publishedVersion().orElseThrow()
                .sampleData());
        assertThat(result).isEqualTo(
                new PublicationResult.Published(new VersionReference(letterhead.id(), TWO)));
        assertThat(templates.letterhead(letterhead.id()).orElseThrow().versions())
                .extracting(TemplateVersion::state)
                .containsExactly(TemplateVersionState.SUPERSEDED, TemplateVersionState.PUBLISHED);
    }

    @Test
    void ifTheProofPdfFailsTheCheckTheVersionStaysADraft() {
        checker.result = pdf -> Conformance.notConformant(List.of("Bild ohne Alternativtext"));

        var result = publication.publish(VERA, reminder.id());

        assertThat(result).isEqualTo(new PublicationResult.NotConformant(List.of("Bild ohne Alternativtext")));
        assertThatReminderUnchanged();
    }

    @Test
    void ifTheCheckCannotRunNothingIsPublished() {
        checker.result = pdf -> {
            throw new IllegalStateException("veraPDF nicht erreichbar");
        };

        assertThatIllegalStateException().isThrownBy(() -> publication.publish(VERA, reminder.id()))
                .withMessage("veraPDF nicht erreichbar");
        assertThatReminderUnchanged();
    }

    @Test
    void withoutPermissionNothingIsCheckedAndNothingPublished() {
        var result = publication.publish(MAX, reminder.id());

        assertThat(result).isEqualTo(new PublicationResult.NotAllowed());
        assertThat(renderer.pdfs).isZero();
        assertThatReminderUnchanged();
    }

    @Test
    void withoutDraftThereIsNothingToPublish() {
        templates.store(reminder.publishDraft());
        templates.store(letterhead.withdraw().newDraft("<header/>").publishDraft());

        assertThat(publication.publish(VERA, reminder.id()))
                .isEqualTo(new PublicationResult.NoDraft());
        assertThat(publication.publish(VERA, letterhead.id()))
                .as("ebenso beim Briefpapier").isEqualTo(new PublicationResult.NoDraft());
        assertThat(renderer.pdfs).isZero();
    }

    @Test
    void aDraftWithoutContentIsNotPublished() {
        templates.store(reminder.changeDraft("  "));
        templates.store(letterhead.newDraft(""));

        assertThat(publication.publish(VERA, reminder.id()))
                .isEqualTo(new PublicationResult.DraftWithoutContent());
        assertThat(publication.publish(VERA, letterhead.id()))
                .as("ebenso beim Briefpapier").isEqualTo(new PublicationResult.DraftWithoutContent());
        assertThat(renderer.pdfs).isZero();
    }

    @Test
    void withoutPublishedLetterheadTheLetterTemplateIsNotPublished() {
        templates.store(letterhead.withdraw());

        var result = publication.publish(VERA, reminder.id());

        assertThat(result).isEqualTo(new PublicationResult.LetterheadNotPublished());
        assertThat(renderer.pdfs).isZero();
        assertThatReminderUnchanged();
    }

    @Test
    void anUnknownTemplateIsReported() {
        assertThat(publication.publish(VERA, TemplateId.generate()))
                .isEqualTo(new PublicationResult.TemplateUnknown());
    }

    @Test
    void ifSomeoneElseChangedTheTemplateMeanwhileNothingIsPublished() {
        LetterTemplate unchecked = reminder.changeDraft("<p>ungeprüft</p>");
        templates.before = () -> templates.store(unchecked);

        var result = publication.publish(VERA, reminder.id());

        assertThat(result).isEqualTo(new PublicationResult.Stale());
        assertThat(savedReminder().draft()).get().extracting(TemplateVersion::content)
                .isEqualTo("<p>ungeprüft</p>");
        assertThat(templates.replacements).isZero();
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"durch Version 2 abgelöst", "zurückgezogen"})
    void ifTheLetterheadChangesDuringTheCheckTheCheckTimeCounts(String change) {
        Letterhead changed = change.equals("zurückgezogen")
                ? letterhead.withdraw()
                : letterhead.newDraft("<header>2</header>").publishDraft();
        List<String> checked = new ArrayList<>();
        checker.result = pdf -> {
            checked.add(new String(pdf, StandardCharsets.UTF_8));
            templates.store(changed);
            return Conformance.conformant();
        };

        var result = publication.publish(VERA, reminder.id());

        assertThat(checked).singleElement().asString().startsWith("<header/>");
        assertThat(result).isEqualTo(
                new PublicationResult.Published(new VersionReference(reminder.id(), TWO)));
        assertThat(templates.letterhead(letterhead.id()).orElseThrow().publishedVersion())
                .map(TemplateVersion::number)
                .isEqualTo(change.equals("zurückgezogen") ? Optional.empty() : Optional.of(TWO));
    }

    @Test
    void aPublishedVersionStaysUnchangedAChangeStartsANewDraft() {
        publication.publish(VERA, reminder.id());
        LetterTemplate published = savedReminder();

        assertThatThrownBy(() -> published.changeDraft("<p>anders</p>"))
                .isInstanceOf(IllegalStateTransition.class);
        TemplateVersion newDraft = published.newDraft("<p>anders</p>").draft().orElseThrow();
        assertThat(newDraft.number()).isEqualTo(new VersionNumber(3));
        assertThat(newDraft.sampleData()).isEqualTo(reminder.draft().orElseThrow().sampleData());
    }

    private LetterTemplate savedReminder() {
        return templates.letterTemplate(reminder.id()).orElseThrow();
    }

    private void assertThatReminderUnchanged() {
        assertThat(templates.replacements).isZero();
        assertThat(savedReminder().versions()).extracting(TemplateVersion::state)
                .containsExactly(TemplateVersionState.PUBLISHED, TemplateVersionState.DRAFT);
    }
}
