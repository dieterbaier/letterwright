package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.validation.Validate;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Vorlage für das Seitenlayout eines Schreibens. Aggregate Root über seine {@link TemplateVersion Versionen}: Jede
 * Änderung an einer Version geht über das Briefpapier, das die Regeln über alle Versionen hinweg sichert (siehe
 * {@link VersionSequence}).
 *
 * <p>Der Inhalt, auch die Stilvorgaben, steht in den Versionen; wie die Pflege die Stilvorgaben strukturiert, legt #8
 * fest. Jeder Übergang liefert eine neue Instanz.
 */
@AggregateRoot
public final class Letterhead {

    @Identity
    private final TemplateId id;
    private final String name;
    private final LetterheadType type;
    private final VersionSequence versions;

    private Letterhead(TemplateId id, String name, LetterheadType type, VersionSequence versions) {
        this.id = Validate.required(id, "Die ID des Briefpapiers");
        this.name = Validate.notBlank(name, "Der Name des Briefpapiers");
        this.type = Validate.required(type, "Der Typ des Briefpapiers");
        this.versions = versions;
    }

    /** Legt ein neues Briefpapier mit der ersten Version als Entwurf an. */
    public static Letterhead create(TemplateId id, String name, LetterheadType type, String content) {
        return new Letterhead(id, name, type, VersionSequence.first(Validate.required(id, "Die ID des Briefpapiers"),
                content));
    }

    /** Legt ein neues Briefpapier mit dem Standardtyp (geschäftlich Form B) an. */
    public static Letterhead createWithDefaultType(TemplateId id, String name, String content) {
        return create(id, name, LetterheadType.STANDARD, content);
    }

    /** Stellt ein gespeichertes Briefpapier mit seinen Versionen wieder her und prüft deren Regeln. */
    public static Letterhead restore(
            TemplateId id, String name, LetterheadType type, List<TemplateVersion> versions) {
        return new Letterhead(id, name, type, VersionSequence.restore(
                Validate.required(id, "Die ID des Briefpapiers"), versions));
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

    /** Alle Versionen, aufsteigend nach Nummer. */
    public List<TemplateVersion> versions() {
        return versions.all();
    }

    public Optional<TemplateVersion> version(VersionNumber number) {
        return versions.version(number);
    }

    public Optional<TemplateVersion> draft() {
        return versions.draft();
    }

    /** Die Version, mit der neue Schreiben entstehen; je Vorlage gibt es höchstens eine (ADR-022). */
    public Optional<TemplateVersion> publishedVersion() {
        return versions.published();
    }

    /** Ersetzt den Inhalt des Entwurfs und behält seine Beispieldaten. Leer ist erlaubt, fehlend nicht. */
    public Letterhead changeDraft(String content) {
        return with(versions.changeDraft(content));
    }

    /** Ersetzt Inhalt und Beispieldaten des Entwurfs. */
    public Letterhead changeDraft(String content, DataContext sampleData) {
        return with(versions.changeDraft(content, sampleData));
    }

    /**
     * Veröffentlicht den Entwurf. Er braucht Inhalt; danach ist er unveränderlich. Die bisher veröffentlichte Version ist
     * danach abgelöst (ADR-022).
     */
    public Letterhead publishDraft() {
        return with(versions.publishDraft());
    }

    /**
     * Legt die nächste Version als Entwurf an und übernimmt die Beispieldaten der neuesten. Geht nur, solange es keinen
     * Entwurf gibt.
     */
    public Letterhead newDraft(String content) {
        return with(versions.newDraft(content));
    }

    /** Nimmt die veröffentlichte Version ohne Nachfolger zurück; bis zur nächsten Veröffentlichung gibt es keine. */
    public Letterhead withdraw() {
        return with(versions.withdraw());
    }

    private Letterhead with(VersionSequence newVersions) {
        return new Letterhead(id, name, type, newVersions);
    }

    /** Gleich sind zwei Instanzen mit derselben ID, auch in verschiedenen Ständen. */
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
        return "Briefpapier[id=" + id.value() + ", name=" + name + "]";
    }
}
