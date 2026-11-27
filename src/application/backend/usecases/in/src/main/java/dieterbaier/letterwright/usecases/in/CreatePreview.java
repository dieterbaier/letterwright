package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.template.TemplateId;

/**
 * Use case "create preview": a clerk sees a letter template for a business reference before saving it. Every preview
 * creates a snapshot (ADR-007).
 */
public interface CreatePreview {

    PreviewResult create(UserId user, BusinessReference reference, TemplateId letterTemplate);

    /**
     * Like {@link #create}, but replaces the snapshot of the previous preview. If it belongs to another user, does not
     * exist or has already produced a letter, it stays untouched.
     */
    PreviewResult update(
            UserId user, BusinessReference reference, TemplateId letterTemplate, SnapshotId previous);
}
