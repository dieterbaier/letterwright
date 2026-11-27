package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.validation.Validate;
import java.time.Instant;

/** The outcome of creating a preview. Only {@link Created} contains a preview. */
public sealed interface PreviewResult {

    /**
     * The preview as HTML, not yet paginated. The snapshot ID is used to save the letter later, until the expiry.
     */
    record Created(SnapshotId snapshot, String html, Instant expiry) implements PreviewResult {

        public Created {
            Validate.required(snapshot, "The snapshot of the preview");
            Validate.required(html, "The HTML of the preview");
            Validate.required(expiry, "The expiry of the preview");
        }
    }

    /** There is no letter template with this ID. */
    record LetterTemplateUnknown() implements PreviewResult {}

    /** The letter template belongs to another reference type (ADR-020). The host was not asked. */
    record ReferenceMismatch() implements PreviewResult {}

    /** The letter template or the letterhead has no published version. */
    record NoPublishedVersion() implements PreviewResult {}

    /** The host does not allow the user to create a letter from the letter template for the reference. */
    record NotAllowed() implements PreviewResult {}
}
