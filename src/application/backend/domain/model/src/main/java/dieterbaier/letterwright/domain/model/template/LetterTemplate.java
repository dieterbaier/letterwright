package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.validation.Validate;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Template for the content of a letter with placeholders. It is assigned exactly one letterhead. Aggregate root over
 * its {@link TemplateVersion versions}, with the same rules as the letterhead (see {@link VersionSequence}). The
 * letterhead is an aggregate of its own and is referenced only by its ID.
 *
 * <p>A letter template belongs to exactly one reference type, such as {@code Customer}. No letter is created from it
 * for a business reference of another type, so that the host never has to supply placeholder data for an unsuitable
 * combination (ADR-020).
 */
@AggregateRoot
public final class LetterTemplate {

    @Identity
    private final TemplateId id;
    private final String name;
    private final TemplateId letterhead;
    private final String referenceType;
    private final VersionSequence versions;

    private LetterTemplate(
            TemplateId id, String name, TemplateId letterhead, String referenceType, VersionSequence versions) {
        this.id = Validate.required(id, "The ID of the letter template");
        this.name = Validate.notBlank(name, "The name of the letter template");
        this.letterhead = Validate.required(letterhead, "The letterhead of the letter template");
        this.referenceType = Validate.notBlank(referenceType, "The reference type of the letter template");
        this.versions = versions;
    }

    /** Creates a new letter template with the first version as a draft. */
    public static LetterTemplate create(
            TemplateId id, String name, TemplateId letterhead, String referenceType, String content) {
        return new LetterTemplate(id, name, letterhead, referenceType, VersionSequence.first(
                Validate.required(id, "The ID of the letter template"), content));
    }

    /** Restores a stored letter template with its versions and checks their rules. */
    public static LetterTemplate restore(
            TemplateId id, String name, TemplateId letterhead, String referenceType, List<TemplateVersion> versions) {
        return new LetterTemplate(id, name, letterhead, referenceType, VersionSequence.restore(
                Validate.required(id, "The ID of the letter template"), versions));
    }

    public TemplateId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public TemplateId letterhead() {
        return letterhead;
    }

    public String referenceType() {
        return referenceType;
    }

    /** Whether this letter template may create a letter for the reference: only for the same reference type. */
    public boolean matches(BusinessReference reference) {
        return referenceType.equals(reference.type());
    }

    /** All versions, in ascending order of number. */
    public List<TemplateVersion> versions() {
        return versions.all();
    }

    public Optional<TemplateVersion> version(VersionNumber number) {
        return versions.version(number);
    }

    public Optional<TemplateVersion> draft() {
        return versions.draft();
    }

    /** The version new letters are created with; each template has at most one (ADR-022). */
    public Optional<TemplateVersion> publishedVersion() {
        return versions.published();
    }

    /** Replaces the content of the draft and keeps its sample data. Empty is allowed, missing is not. */
    public LetterTemplate changeDraft(String content) {
        return with(versions.changeDraft(content));
    }

    /** Replaces content and sample data of the draft. */
    public LetterTemplate changeDraft(String content, DataContext sampleData) {
        return with(versions.changeDraft(content, sampleData));
    }

    /**
     * Publishes the draft. It needs content; afterwards it is immutable. The previously published version is then
     * superseded (ADR-022).
     */
    public LetterTemplate publishDraft() {
        return with(versions.publishDraft());
    }

    /**
     * Creates the next version as a draft and takes over the sample data of the newest one. Only possible while there
     * is no draft.
     */
    public LetterTemplate newDraft(String content) {
        return with(versions.newDraft(content));
    }

    /** Withdraws the published version without a successor; until the next publication there is none. */
    public LetterTemplate withdraw() {
        return with(versions.withdraw());
    }

    private LetterTemplate with(VersionSequence newVersions) {
        return new LetterTemplate(id, name, letterhead, referenceType, newVersions);
    }

    /** Two instances with the same ID are equal, even in different states. */
    @Override
    public boolean equals(Object other) {
        return other instanceof LetterTemplate template && id.equals(template.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "LetterTemplate[id=" + id.value() + ", name=" + name + "]";
    }
}
