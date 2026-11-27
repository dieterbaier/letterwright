package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.validation.Validate;

/** The outcome of withdrawing a template version. Except for {@link Withdrawn}, everything stays as it was. */
public sealed interface WithdrawalResult {

    record Withdrawn(VersionReference version) implements WithdrawalResult {

        public Withdrawn {
            Validate.required(version, "The withdrawn version");
        }
    }

    record NotAllowed() implements WithdrawalResult {}

    record TemplateUnknown() implements WithdrawalResult {}

    record NothingPublished() implements WithdrawalResult {}

    /** The template has changed since it was loaded, for example by another template manager. */
    record Stale() implements WithdrawalResult {}
}
