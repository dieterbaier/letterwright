package dieterbaier.letterwright.usecases.service;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.TemplateAction;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import dieterbaier.letterwright.domain.model.template.TemplateVersion;
import dieterbaier.letterwright.usecases.in.WithdrawalResult;
import dieterbaier.letterwright.usecases.in.WithdrawTemplateVersion;
import dieterbaier.letterwright.usecases.out.Permissions;
import dieterbaier.letterwright.usecases.out.ReplaceOutcome;
import dieterbaier.letterwright.usecases.out.TemplateRepository;
import java.util.Optional;
import java.util.function.Supplier;

public final class WithdrawTemplateVersionService implements WithdrawTemplateVersion {

    private final TemplateRepository templates;
    private final Permissions permissions;

    public WithdrawTemplateVersionService(TemplateRepository templates, Permissions permissions) {
        this.templates = templates;
        this.permissions = permissions;
    }

    /** Zurückziehen gehört zum Veröffentlichen: Beides bestimmt, was in neue Schreiben eingeht. */
    @Override
    public WithdrawalResult withdraw(UserId user, TemplateId template) {
        if (!permissions.may(user, TemplateAction.PUBLISH)) {
            return new WithdrawalResult.NotAllowed();
        }
        Optional<LetterTemplate> letterTemplate = templates.letterTemplate(template);
        if (letterTemplate.isPresent()) {
            LetterTemplate loaded = letterTemplate.get();
            return loaded.publishedVersion()
                    .map(version -> withdraw(version, () -> templates.replace(loaded, loaded.withdraw())))
                    .orElseGet(WithdrawalResult.NothingPublished::new);
        }
        return templates.letterhead(template)
                .map(loaded -> loaded.publishedVersion()
                        .map(version -> withdraw(version, () -> templates.replace(loaded, loaded.withdraw())))
                        .orElseGet(WithdrawalResult.NothingPublished::new))
                .orElseGet(WithdrawalResult.TemplateUnknown::new);
    }

    private static WithdrawalResult withdraw(TemplateVersion published, Supplier<ReplaceOutcome> replace) {
        return switch (replace.get()) {
            case ReplaceOutcome.Replaced replaced -> new WithdrawalResult.Withdrawn(published.reference());
            case ReplaceOutcome.Stale stale -> new WithdrawalResult.Stale();
        };
    }
}
