package dieterbaier.letterwright.usecases.service;

import static org.assertj.core.api.Assertions.assertThat;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.TemplateAction;
import dieterbaier.letterwright.domain.model.template.Letterhead;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.VersionNumber;
import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import dieterbaier.letterwright.domain.model.template.TemplateVersion;
import dieterbaier.letterwright.domain.model.template.TemplateVersionState;
import dieterbaier.letterwright.usecases.in.WithdrawalResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Bridge to {@code features/templates/withdraw-template-version.feature}: one test per scenario, named after the
 * scenario title. The outbound ports are {@link Fakes}.
 */
class WithdrawTemplateVersionServiceTest {

    private static final UserId VERA = new UserId("vera");
    private static final UserId MAX = new UserId("max");

    private final Fakes.Templates templates = new Fakes.Templates();
    private final Fakes.Host host = new Fakes.Host();
    private final WithdrawTemplateVersionService withdrawal = new WithdrawTemplateVersionService(templates, host);

    // Background: a letterhead and a letter template, each with published version 1 and draft 2; Vera may
    // publish.
    private final Letterhead letterhead = templates.store(Letterhead.createWithDefaultType(TemplateId.generate(),
            "Standard", "<header/>").publishDraft().newDraft("<header>2</header>"));
    private final LetterTemplate reminder = templates.store(LetterTemplate.create(TemplateId.generate(), "Payment reminder",
            letterhead.id(), "Customer", "<p>1</p>").publishDraft().newDraft("<p>2</p>"));

    {
        host.mayPublish.add(VERA);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"a letter template", "a letterhead"})
    void thePublishedVersionIsWithdrawnWithoutSuccessor(String template) {
        TemplateId id = id(template);

        var result = withdrawal.withdraw(VERA, id);

        assertThat(result).isEqualTo(
                new WithdrawalResult.Withdrawn(new VersionReference(id, VersionNumber.FIRST)));
        assertThat(versions(template)).extracting(TemplateVersion::state)
                .containsExactly(TemplateVersionState.WITHDRAWN, TemplateVersionState.DRAFT);
        assertThat(versions(template).getLast().content()).as("the draft stays unchanged")
                .isEqualTo(template.contains("letterhead") ? "<header>2</header>" : "<p>2</p>");
        assertThat(host.templateActionRequested).containsExactly(TemplateAction.PUBLISH);
    }

    @Test
    void withoutPermissionNothingIsWithdrawn() {
        var result = withdrawal.withdraw(MAX, reminder.id());

        assertThat(result).isEqualTo(new WithdrawalResult.NotAllowed());
        assertThat(templates.replacements).isZero();
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"a letter template", "a letterhead"})
    void withoutPublishedVersionThereIsNothingToWithdraw(String template) {
        templates.store(Letterhead.createWithDefaultType(letterhead.id(), "Standard", "<header/>"));
        templates.store(LetterTemplate.create(reminder.id(), "Payment reminder", letterhead.id(), "Customer", "<p>1</p>"));

        var result = withdrawal.withdraw(VERA, id(template));

        assertThat(result).isEqualTo(new WithdrawalResult.NothingPublished());
        assertThat(templates.replacements).isZero();
    }

    @Test
    void anUnknownTemplateIsReported() {
        assertThat(withdrawal.withdraw(VERA, TemplateId.generate())).isEqualTo(new WithdrawalResult.TemplateUnknown());
    }

    @Test
    void ifSomeoneElseChangedTheTemplateMeanwhileNothingIsWithdrawn() {
        templates.before = () -> templates.store(reminder.publishDraft());

        var result = withdrawal.withdraw(VERA, reminder.id());

        assertThat(result).isEqualTo(new WithdrawalResult.Stale());
        assertThat(versions("a letter template")).extracting(TemplateVersion::state)
                .containsExactly(TemplateVersionState.SUPERSEDED, TemplateVersionState.PUBLISHED);
    }

    private TemplateId id(String template) {
        return template.contains("letterhead") ? letterhead.id() : reminder.id();
    }

    private List<TemplateVersion> versions(String template) {
        return template.contains("letterhead")
                ? templates.letterhead(letterhead.id()).orElseThrow().versions()
                : templates.letterTemplate(reminder.id()).orElseThrow().versions();
    }
}
