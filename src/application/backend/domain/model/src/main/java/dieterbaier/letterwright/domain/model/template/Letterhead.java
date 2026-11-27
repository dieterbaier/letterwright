package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.validation.Validate;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Template for the page layout of a letter. Aggregate root over its {@link TemplateVersion versions}: every change to a
 * version goes through the letterhead, which secures the rules across all versions (see {@link VersionSequence}).
 *
 * <p>The content, including the style rules, is in the versions; how editing structures the style rules is decided by
 * #8. Every transition returns a new instance.
 */
@AggregateRoot
public final class Letterhead {

    @Identity
    private final TemplateId id;
    private final String name;
    private final LetterheadType type;
    private final VersionSequence versions;

    private Letterhead(TemplateId id, String name, LetterheadType type, VersionSequence versions) {
        this.id = Validate.required(id, "The ID of the letterhead");
        this.name = Validate.notBlank(name, "The name of the letterhead");
        this.type = Validate.required(type, "The type of the letterhead");
        this.versions = versions;
    }

    /** Creates a new letterhead with the first version as a draft. */
    public static Letterhead create(TemplateId id, String name, LetterheadType type, String content) {
        return new Letterhead(id, name, type, VersionSequence.first(Validate.required(id, "The ID of the letterhead"),
                content));
    }

    /** Creates a new letterhead with the default type (business form B). */
    public static Letterhead createWithDefaultType(TemplateId id, String name, String content) {
        return create(id, name, LetterheadType.STANDARD, content);
    }

    /** Restores a stored letterhead with its versions and checks their rules. */
    public static Letterhead restore(
            TemplateId id, String name, LetterheadType type, List<TemplateVersion> versions) {
        return new Letterhead(id, name, type, VersionSequence.restore(
                Validate.required(id, "The ID of the letterhead"), versions));
    }

    public TemplateId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public LetterheadType type() {
        return type;
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
    public Letterhead changeDraft(String content) {
        return with(versions.changeDraft(content));
    }

    /** Replaces content and sample data of the draft. */
    public Letterhead changeDraft(String content, DataContext sampleData) {
        return with(versions.changeDraft(content, sampleData));
    }

    /**
     * Publishes the draft. It needs content; afterwards it is immutable. The previously published version is then
     * superseded (ADR-022).
     */
    public Letterhead publishDraft() {
        return with(versions.publishDraft());
    }

    /**
     * Creates the next version as a draft and takes over the sample data of the newest one. Only possible while there
     * is no draft.
     */
    public Letterhead newDraft(String content) {
        return with(versions.newDraft(content));
    }

    /** Withdraws the published version without a successor; until the next publication there is none. */
    public Letterhead withdraw() {
        return with(versions.withdraw());
    }

    private Letterhead with(VersionSequence newVersions) {
        return new Letterhead(id, name, type, newVersions);
    }

    /** Two instances with the same ID are equal, even in different states. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Letterhead template && id.equals(template.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Letterhead[id=" + id.value() + ", name=" + name + "]";
    }
}
