package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Ein Stand eines Briefpapiers oder Standardschreibens (ADR-007). Der Inhalt ist semantisches HTML mit CSS Paged Media
 * (ADR-003).
 *
 * <p>Entität im Aggregat ihrer Vorlage: Zustandswechsel gibt es nur über {@link Letterhead} bzw.
 * {@link LetterTemplate}, die dabei die Regeln über alle Versionen hinweg sichern. Gleich sind zwei Instanzen, wenn
 * sie dieselbe Version derselben Vorlage meinen, auch in verschiedenen Zuständen.
 *
 * <p>Der Inhalt darf leer sein, solange die Version ein Entwurf ist. Veröffentlichen verlangt Inhalt.
 *
 * <p>Die Beispieldaten füllen die Platzhalter im Probe-PDF, das vor dem Veröffentlichen geprüft wird. Sie gehören zur
 * Version, damit nachvollziehbar bleibt, womit eine veröffentlichte Version geprüft wurde (ADR-022).
 */
@Entity
public final class TemplateVersion {

    private final TemplateId template;
    private final VersionNumber number;
    private final TemplateVersionState state;
    private final String content;
    private final DataContext sampleData;

    private TemplateVersion(TemplateId template, VersionNumber number, TemplateVersionState state, String content,
            DataContext sampleData) {
        this.template = Validate.required(template, "Die Vorlage der Version");
        this.number = Validate.required(number, "Die Versionsnummer");
        this.state = Validate.required(state, "Der Zustand der Version");
        this.content = Validate.required(content, "Der Inhalt der Version");
        this.sampleData = Validate.required(sampleData, "Die Beispieldaten der Version");
        if (state != TemplateVersionState.DRAFT) {
            Validate.notBlank(content, "Der Inhalt einer freigegebenen Version");
        }
    }

    public static TemplateVersion restore(TemplateId template, VersionNumber number,
            TemplateVersionState state, String content, DataContext sampleData) {
        return new TemplateVersion(template, number, state, content, sampleData);
    }

    static TemplateVersion draft(TemplateId template, VersionNumber number, String content,
            DataContext sampleData) {
        return new TemplateVersion(template, number, TemplateVersionState.DRAFT, content, sampleData);
    }

    TemplateVersion withContent(String newContent, DataContext newSampleData) {
        return new TemplateVersion(template, number, state, newContent, newSampleData);
    }

    TemplateVersion inState(TemplateVersionState newState) {
        return new TemplateVersion(template, number, newState, content, sampleData);
    }

    public TemplateId template() {
        return template;
    }

    public VersionNumber number() {
        return number;
    }

    public TemplateVersionState state() {
        return state;
    }

    public String content() {
        return content;
    }

    public DataContext sampleData() {
        return sampleData;
    }

    /** Ein Entwurf mit Inhalt, der nicht nur aus Leerraum besteht. */
    public boolean isPublishable() {
        return state == TemplateVersionState.DRAFT && !content.isBlank();
    }

    /**
     * Die Identität der Version: Vorlage und Nummer. Snapshot und Schreiben speichern genau diesen Verweis.
     */
    @Identity
    public VersionReference reference() {
        return new VersionReference(template, number);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TemplateVersion version && reference().equals(version.reference());
    }

    @Override
    public int hashCode() {
        return reference().hashCode();
    }

    @Override
    public String toString() {
        return "Vorlagenversion[vorlage=" + template.value() + ", nummer=" + number.value() + ", zustand=" + state + "]";
    }
}
