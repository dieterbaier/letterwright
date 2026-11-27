package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.template.VersionReference;
import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import java.util.List;

/** Was beim Veröffentlichen einer Vorlagenversion herauskommt. Außer bei {@link Published} bleibt alles, wie es war. */
public sealed interface PublicationResult {

    /** Diese Version ist jetzt die veröffentlichte; neue Schreiben entstehen mit ihr. */
    record Published(VersionReference version) implements PublicationResult {

        public Published {
            Validate.required(version, "Die veröffentlichte Version");
        }
    }

    /** Das Probe-PDF ist nicht konform; der Entwurf bleibt ein Entwurf. Die Befunde sind für den Vorlagenverwalter. */
    record NotConformant(List<String> findings) implements PublicationResult {

        public NotConformant {
            findings = List.copyOf(Validate.requiredWithoutGaps(findings, "Die Befunde der Konformitätsprüfung"));
            if (findings.isEmpty()) {
                throw new ValidationException("Ein nicht konformes Probe-PDF braucht mindestens einen Befund");
            }
        }
    }

    record NotAllowed() implements PublicationResult {}

    record TemplateUnknown() implements PublicationResult {}

    record NoDraft() implements PublicationResult {}

    /** Der Entwurf ist leer oder enthält nur Leerraum. */
    record DraftWithoutContent() implements PublicationResult {}

    /** Ein Standardschreiben braucht für sein Probe-PDF die veröffentlichte Version seines Briefpapiers. */
    record LetterheadNotPublished() implements PublicationResult {}

    /** Die Vorlage hat sich seit dem Laden geändert, etwa durch einen anderen Vorlagenverwalter. Neu laden und prüfen. */
    record Stale() implements PublicationResult {}
}
