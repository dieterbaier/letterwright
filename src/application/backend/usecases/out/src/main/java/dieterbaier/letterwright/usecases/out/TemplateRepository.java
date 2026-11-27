package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.template.Letterhead;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import java.util.Optional;

/** Loads and replaces letterheads and letter templates with all their versions. */
public interface TemplateRepository {

    Optional<LetterTemplate> letterTemplate(TemplateId id);

    Optional<Letterhead> letterhead(TemplateId id);

    /**
     * Replaces the letter template with {@code changed}, in one step and only if the stored state with all versions,
     * states, contents and sample data still equals {@code loaded}. Otherwise it replaces nothing. This way, content
     * that was not checked is never published (ADR-022).
     *
     * <p>An exception means: whether anything was replaced is uncertain.
     */
    ReplaceOutcome replace(LetterTemplate loaded, LetterTemplate changed);

    /** Like {@link #replace(LetterTemplate, LetterTemplate)}, for a letterhead. */
    ReplaceOutcome replace(Letterhead loaded, Letterhead changed);
}
