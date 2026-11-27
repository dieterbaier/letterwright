package dieterbaier.letterwright.usecases.service;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.TemplateAction;
import dieterbaier.letterwright.domain.model.template.Letterhead;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import dieterbaier.letterwright.domain.model.template.TemplateVersion;
import dieterbaier.letterwright.usecases.in.PublicationResult;
import dieterbaier.letterwright.usecases.in.PublishTemplateVersion;
import dieterbaier.letterwright.usecases.out.Permissions;
import dieterbaier.letterwright.usecases.out.ReplaceOutcome;
import dieterbaier.letterwright.usecases.out.Conformance;
import dieterbaier.letterwright.usecases.out.ConformanceChecker;
import dieterbaier.letterwright.usecases.out.Renderer;
import dieterbaier.letterwright.usecases.out.TemplateRepository;
import java.util.Optional;
import java.util.function.Supplier;

public final class PublishTemplateVersionService implements PublishTemplateVersion {

    private final TemplateRepository templates;
    private final Permissions permissions;
    private final Renderer renderer;
    private final ConformanceChecker checker;

    public PublishTemplateVersionService(TemplateRepository templates, Permissions permissions, Renderer renderer,
            ConformanceChecker checker) {
        this.templates = templates;
        this.permissions = permissions;
        this.renderer = renderer;
        this.checker = checker;
    }

    @Override
    public PublicationResult publish(UserId user, TemplateId template) {
        if (!permissions.may(user, TemplateAction.PUBLISH)) {
            return new PublicationResult.NotAllowed();
        }
        Optional<LetterTemplate> letterTemplate = templates.letterTemplate(template);
        if (letterTemplate.isPresent()) {
            return publish(letterTemplate.get());
        }
        return templates.letterhead(template)
                .map(this::publish)
                .orElseGet(PublicationResult.TemplateUnknown::new);
    }

    private PublicationResult publish(LetterTemplate loaded) {
        Optional<TemplateVersion> draft = loaded.draft();
        if (draft.isEmpty()) {
            return new PublicationResult.NoDraft();
        }
        if (!draft.get().isPublishable()) {
            return new PublicationResult.DraftWithoutContent();
        }
        Optional<TemplateVersion> letterhead = templates.letterhead(loaded.letterhead())
                .flatMap(Letterhead::publishedVersion);
        if (letterhead.isEmpty()) {
            return new PublicationResult.LetterheadNotPublished();
        }
        var sampleData = letterhead.get().sampleData().extendedBy(draft.get().sampleData());
        byte[] proof = renderer.createPdf(letterhead.get(), draft.get(), sampleData);
        return ifConformant(proof, draft.get(),
                () -> templates.replace(loaded, loaded.publishDraft()));
    }

    private PublicationResult publish(Letterhead loaded) {
        Optional<TemplateVersion> draft = loaded.draft();
        if (draft.isEmpty()) {
            return new PublicationResult.NoDraft();
        }
        if (!draft.get().isPublishable()) {
            return new PublicationResult.DraftWithoutContent();
        }
        byte[] proof = renderer.createProofPdf(draft.get(), draft.get().sampleData());
        return ifConformant(proof, draft.get(),
                () -> templates.replace(loaded, loaded.publishDraft()));
    }

    /** Kann der Prüfer nicht prüfen, wirft er; dann wird ebenfalls nichts ersetzt (fail-closed, ADR-005). */
    private PublicationResult ifConformant(
            byte[] proof, TemplateVersion draft, Supplier<ReplaceOutcome> replace) {
        if (checker.check(proof) instanceof Conformance.NotConformant notConformant) {
            return new PublicationResult.NotConformant(notConformant.findings());
        }
        return switch (replace.get()) {
            case ReplaceOutcome.Replaced replaced -> new PublicationResult.Published(draft.reference());
            case ReplaceOutcome.Stale stale -> new PublicationResult.Stale();
        };
    }
}
