package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

/**
 * A revision of a letterhead or letter template (ADR-007). The content is semantic HTML with CSS Paged Media
 * (ADR-003).
 *
 * <p>Entity in the aggregate of its template: state changes happen only through {@link Letterhead} or
 * {@link LetterTemplate}, which secure the rules across all versions. Two instances are equal if they denote the same
 * version of the same template, even in different states.
 *
 * <p>The content may be empty while the version is a draft. Publishing requires content.
 *
 * <p>The sample data fills the placeholders in the proof PDF that is checked before publishing. It belongs to the
 * version so that it stays traceable what a published version was checked with (ADR-022).
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
        this.template = Validate.required(template, "The template of the version");
        this.number = Validate.required(number, "The VersionNumber");
        this.state = Validate.required(state, "The state of the version");
        this.content = Validate.required(content, "The content of the version");
        this.sampleData = Validate.required(sampleData, "The sample data of the version");
        if (state != TemplateVersionState.DRAFT) {
            Validate.notBlank(content, "The content of a released version");
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

    /** A draft whose content is not only whitespace. */
    public boolean isPublishable() {
        return state == TemplateVersionState.DRAFT && !content.isBlank();
    }

    /**
     * The identity of the version: template and number. Snapshot and letter store exactly this reference.
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
        return "TemplateVersion[template=" + template.value() + ", number=" + number.value() + ", state=" + state + "]";
    }
}
