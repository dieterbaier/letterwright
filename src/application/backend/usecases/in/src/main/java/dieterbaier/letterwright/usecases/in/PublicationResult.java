package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import java.util.List;

/** The outcome of publishing a template version. Except for {@link Published}, everything stays as it was. */
public sealed interface PublicationResult {

    /** This version is now the published one; new letters are created with it. */
    record Published(VersionReference version) implements PublicationResult {

        public Published {
            Validate.required(version, "The published version");
        }
    }

    /** The proof PDF is not conformant; the draft stays a draft. The findings are meant for the template manager. */
    record NotConformant(List<String> findings) implements PublicationResult {

        public NotConformant {
            findings = List.copyOf(Validate.requiredWithoutGaps(findings, "The findings of the conformance check"));
            if (findings.isEmpty()) {
                throw new ValidationException("A non-conformant proof PDF needs at least one finding");
            }
        }
    }

    record NotAllowed() implements PublicationResult {}

    record TemplateUnknown() implements PublicationResult {}

    record NoDraft() implements PublicationResult {}

    /** The draft is empty or contains only whitespace. */
    record DraftWithoutContent() implements PublicationResult {}

    /** A letter template needs the published version of its letterhead for its proof PDF. */
    record LetterheadNotPublished() implements PublicationResult {}

    /** The template has changed since it was loaded, for example by another template manager. Reload and check. */
    record Stale() implements PublicationResult {}
}
