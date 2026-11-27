package dieterbaier.letterwright.usecases.service;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.snapshot.Snapshot;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import dieterbaier.letterwright.domain.model.template.TemplateVersion;
import dieterbaier.letterwright.usecases.in.CreatePreview;
import dieterbaier.letterwright.usecases.in.PreviewResult;
import dieterbaier.letterwright.usecases.out.Permissions;
import dieterbaier.letterwright.usecases.out.PlaceholderDataSource;
import dieterbaier.letterwright.usecases.out.Renderer;
import dieterbaier.letterwright.usecases.out.SnapshotStore;
import dieterbaier.letterwright.usecases.out.TemplateRepository;
import java.time.Clock;
import java.util.Optional;

/**
 * First checks whether the letter template exists and whether its reference type matches, then asks the host for
 * permission. Only with permission does it check the published versions, so that an unauthorized user does not learn
 * their state, and request the data context (ADR-020).
 */
public final class CreatePreviewService implements CreatePreview {

    private final TemplateRepository templates;
    private final Permissions permissions;
    private final PlaceholderDataSource data;
    private final Renderer renderer;
    private final SnapshotStore snapshots;
    private final Clock clock;

    public CreatePreviewService(TemplateRepository templates, Permissions permissions, PlaceholderDataSource data,
            Renderer renderer, SnapshotStore snapshots, Clock clock) {
        this.templates = templates;
        this.permissions = permissions;
        this.data = data;
        this.renderer = renderer;
        this.snapshots = snapshots;
        this.clock = clock;
    }

    @Override
    public PreviewResult create(UserId user, BusinessReference reference, TemplateId letterTemplate) {
        return templates.letterTemplate(letterTemplate)
                .map(found -> create(user, reference, found))
                .orElseGet(PreviewResult.LetterTemplateUnknown::new);
    }

    @Override
    public PreviewResult update(
            UserId user, BusinessReference reference, TemplateId letterTemplate, SnapshotId previous) {
        PreviewResult result = create(user, reference, letterTemplate);
        if (result instanceof PreviewResult.Created) {
            snapshots.load(previous)
                    .filter(snapshot -> snapshot.user().equals(user))
                    .ifPresent(own -> snapshots.deleteIfOpen(own.id()));
        }
        return result;
    }

    private PreviewResult create(UserId user, BusinessReference reference, LetterTemplate template) {
        if (!template.matches(reference)) {
            return new PreviewResult.ReferenceMismatch();
        }
        if (!permissions.mayCreate(user, reference, template)) {
            return new PreviewResult.NotAllowed();
        }
        Optional<TemplateVersion> content = template.publishedVersion();
        Optional<TemplateVersion> letterhead = templates.letterhead(template.letterhead())
                .flatMap(found -> found.publishedVersion());
        if (content.isEmpty() || letterhead.isEmpty()) {
            return new PreviewResult.NoPublishedVersion();
        }
        var dataContext = data.readDataContext(user, reference, template);
        var snapshot = Snapshot.create(SnapshotId.generate(), user, reference, letterhead.get().reference(),
                content.get().reference(), dataContext, clock.instant());
        String html = renderer.merge(letterhead.get(), content.get(), dataContext);
        snapshots.save(snapshot);
        return new PreviewResult.Created(snapshot.id(), html, snapshot.expiry());
    }
}
