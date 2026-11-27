package dieterbaier.letterwright.domain.model.template;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.validation.Validate;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Vorlage für den Inhalt eines Schreibens mit Platzhaltern. Ihr ist genau ein Briefpapier zugewiesen. Aggregate Root
 * über ihre {@link TemplateVersion Versionen}, mit denselben Regeln wie das Briefpapier (siehe {@link VersionSequence}).
 * Das Briefpapier ist ein eigenes Aggregat und wird nur über seine ID referenziert.
 *
 * <p>Ein Standardschreiben gehört zu genau einem Bezugstyp, etwa {@code Kunde}. Zu einem fachlichen Bezug anderen Typs
 * entsteht kein Schreiben daraus, damit der Host keine Platzhalterdaten für eine unpassende Kombination liefern muss
 * (ADR-020).
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
        this.id = Validate.required(id, "Die ID des Standardschreibens");
        this.name = Validate.notBlank(name, "Der Name des Standardschreibens");
        this.letterhead = Validate.required(letterhead, "Das Briefpapier des Standardschreibens");
        this.referenceType = Validate.notBlank(referenceType, "Der Bezugstyp des Standardschreibens");
        this.versions = versions;
    }

    /** Legt ein neues Standardschreiben mit der ersten Version als Entwurf an. */
    public static LetterTemplate create(
            TemplateId id, String name, TemplateId letterhead, String referenceType, String content) {
        return new LetterTemplate(id, name, letterhead, referenceType, VersionSequence.first(
                Validate.required(id, "Die ID des Standardschreibens"), content));
    }

    /** Stellt ein gespeichertes Standardschreiben mit seinen Versionen wieder her und prüft deren Regeln. */
    public static LetterTemplate restore(
            TemplateId id, String name, TemplateId letterhead, String referenceType, List<TemplateVersion> versions) {
        return new LetterTemplate(id, name, letterhead, referenceType, VersionSequence.restore(
                Validate.required(id, "Die ID des Standardschreibens"), versions));
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

    /** Ob aus diesem Standardschreiben zu dem Bezug ein Schreiben entstehen darf: nur bei gleichem Bezugstyp. */
    public boolean matches(BusinessReference reference) {
        return referenceType.equals(reference.type());
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
    public LetterTemplate changeDraft(String content) {
        return with(versions.changeDraft(content));
    }

    /** Ersetzt Inhalt und Beispieldaten des Entwurfs. */
    public LetterTemplate changeDraft(String content, DataContext sampleData) {
        return with(versions.changeDraft(content, sampleData));
    }

    /**
     * Veröffentlicht den Entwurf. Er braucht Inhalt; danach ist er unveränderlich. Die bisher veröffentlichte Version ist
     * danach abgelöst (ADR-022).
     */
    public LetterTemplate publishDraft() {
        return with(versions.publishDraft());
    }

    /**
     * Legt die nächste Version als Entwurf an und übernimmt die Beispieldaten der neuesten. Geht nur, solange es keinen
     * Entwurf gibt.
     */
    public LetterTemplate newDraft(String content) {
        return with(versions.newDraft(content));
    }

    /** Nimmt die veröffentlichte Version ohne Nachfolger zurück; bis zur nächsten Veröffentlichung gibt es keine. */
    public LetterTemplate withdraw() {
        return with(versions.withdraw());
    }

    private LetterTemplate with(VersionSequence newVersions) {
        return new LetterTemplate(id, name, letterhead, referenceType, newVersions);
    }

    /** Gleich sind zwei Instanzen mit derselben ID, auch in verschiedenen Ständen. */
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
        return "Standardschreiben[id=" + id.value() + ", name=" + name + "]";
    }
}
