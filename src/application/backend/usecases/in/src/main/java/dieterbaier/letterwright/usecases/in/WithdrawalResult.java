package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.validation.Validate;

/** Was beim Zurückziehen einer Vorlagenversion herauskommt. Außer bei {@link Withdrawn} bleibt alles, wie es war. */
public sealed interface WithdrawalResult {

    record Withdrawn(VersionReference version) implements WithdrawalResult {

        public Withdrawn {
            Validate.required(version, "Die zurückgezogene Version");
        }
    }

    record NotAllowed() implements WithdrawalResult {}

    record TemplateUnknown() implements WithdrawalResult {}

    record NothingPublished() implements WithdrawalResult {}

    /** Die Vorlage hat sich seit dem Laden geändert, etwa durch einen anderen Vorlagenverwalter. */
    record Stale() implements WithdrawalResult {}
}
