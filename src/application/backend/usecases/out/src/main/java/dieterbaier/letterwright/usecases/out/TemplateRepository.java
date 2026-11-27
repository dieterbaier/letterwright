package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.template.Letterhead;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;
import dieterbaier.letterwright.domain.model.template.TemplateId;
import java.util.Optional;

/** Lädt und ersetzt Briefpapiere und Standardschreiben mit all ihren Versionen. */
public interface TemplateRepository {

    Optional<LetterTemplate> letterTemplate(TemplateId id);

    Optional<Letterhead> letterhead(TemplateId id);

    /**
     * Ersetzt das Standardschreiben durch {@code changed}, in einem Schritt und nur, wenn der gespeicherte Stand mit allen
     * Versionen, Zuständen, Inhalten und Beispieldaten noch {@code loaded} entspricht. Sonst ersetzt es nichts. So wird
     * nie ein Inhalt veröffentlicht, der nicht geprüft wurde (ADR-022).
     *
     * <p>Eine Ausnahme heißt: Ob ersetzt wurde, ist ungewiss.
     */
    ReplaceOutcome replace(LetterTemplate loaded, LetterTemplate changed);

    /** Wie {@link #replace(LetterTemplate, LetterTemplate)}, für ein Briefpapier. */
    ReplaceOutcome replace(Letterhead loaded, Letterhead changed);
}
