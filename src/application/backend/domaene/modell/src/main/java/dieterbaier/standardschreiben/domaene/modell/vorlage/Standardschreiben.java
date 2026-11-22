package dieterbaier.standardschreiben.domaene.modell.vorlage;

import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.validierung.Pruefe;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Vorlage für den Inhalt eines Schreibens mit Platzhaltern. Ihr ist genau ein Briefpapier zugewiesen. Aggregate Root
 * über ihre {@link Vorlagenversion Versionen}, mit denselben Regeln wie das Briefpapier (siehe {@link Versionsfolge}).
 * Das Briefpapier ist ein eigenes Aggregat und wird nur über seine ID referenziert.
 *
 * <p>Ein Standardschreiben gehört zu genau einem Bezugstyp, etwa {@code Kunde}. Zu einem fachlichen Bezug anderen Typs
 * entsteht kein Schreiben daraus, damit der Host keine Platzhalterdaten für eine unpassende Kombination liefern muss
 * (ADR-020).
 */
@AggregateRoot
public final class Standardschreiben {

    @Identity
    private final VorlagenId id;
    private final String name;
    private final VorlagenId briefpapier;
    private final String bezugstyp;
    private final Versionsfolge versionen;

    private Standardschreiben(
            VorlagenId id, String name, VorlagenId briefpapier, String bezugstyp, Versionsfolge versionen) {
        this.id = Pruefe.pflicht(id, "Die ID des Standardschreibens");
        this.name = Pruefe.nichtBlank(name, "Der Name des Standardschreibens");
        this.briefpapier = Pruefe.pflicht(briefpapier, "Das Briefpapier des Standardschreibens");
        this.bezugstyp = Pruefe.nichtBlank(bezugstyp, "Der Bezugstyp des Standardschreibens");
        this.versionen = versionen;
    }

    /** Legt ein neues Standardschreiben mit der ersten Version als Entwurf an. */
    public static Standardschreiben anlegen(
            VorlagenId id, String name, VorlagenId briefpapier, String bezugstyp, String inhalt) {
        return new Standardschreiben(id, name, briefpapier, bezugstyp, Versionsfolge.erste(
                Pruefe.pflicht(id, "Die ID des Standardschreibens"), inhalt));
    }

    /** Stellt ein gespeichertes Standardschreiben mit seinen Versionen wieder her und prüft deren Regeln. */
    public static Standardschreiben wiederherstellen(
            VorlagenId id, String name, VorlagenId briefpapier, String bezugstyp, List<Vorlagenversion> versionen) {
        return new Standardschreiben(id, name, briefpapier, bezugstyp, Versionsfolge.wiederherstellen(
                Pruefe.pflicht(id, "Die ID des Standardschreibens"), versionen));
    }

    public VorlagenId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public VorlagenId briefpapier() {
        return briefpapier;
    }

    public String bezugstyp() {
        return bezugstyp;
    }

    /** Ob aus diesem Standardschreiben zu dem Bezug ein Schreiben entstehen darf: nur bei gleichem Bezugstyp. */
    public boolean passtZu(FachlicherBezug bezug) {
        return bezugstyp.equals(bezug.typ());
    }

    /** Alle Versionen, aufsteigend nach Nummer. */
    public List<Vorlagenversion> versionen() {
        return versionen.alle();
    }

    public Optional<Vorlagenversion> version(Versionsnummer nummer) {
        return versionen.version(nummer);
    }

    public Optional<Vorlagenversion> entwurf() {
        return versionen.entwurf();
    }

    /** Die Version, mit der neue Schreiben entstehen; je Vorlage gibt es höchstens eine (ADR-022). */
    public Optional<Vorlagenversion> veroeffentlichteVersion() {
        return versionen.veroeffentlichte();
    }

    /** Ersetzt den Inhalt des Entwurfs und behält seine Beispieldaten. Leer ist erlaubt, fehlend nicht. */
    public Standardschreiben aendereEntwurf(String inhalt) {
        return mit(versionen.aendereEntwurf(inhalt));
    }

    /** Ersetzt Inhalt und Beispieldaten des Entwurfs. */
    public Standardschreiben aendereEntwurf(String inhalt, Datenkontext beispieldaten) {
        return mit(versionen.aendereEntwurf(inhalt, beispieldaten));
    }

    /**
     * Veröffentlicht den Entwurf. Er braucht Inhalt; danach ist er unveränderlich. Die bisher veröffentlichte Version ist
     * danach abgelöst (ADR-022).
     */
    public Standardschreiben veroeffentlicheEntwurf() {
        return mit(versionen.veroeffentlicheEntwurf());
    }

    /**
     * Legt die nächste Version als Entwurf an und übernimmt die Beispieldaten der neuesten. Geht nur, solange es keinen
     * Entwurf gibt.
     */
    public Standardschreiben neuerEntwurf(String inhalt) {
        return mit(versionen.neuerEntwurf(inhalt));
    }

    /** Nimmt die veröffentlichte Version ohne Nachfolger zurück; bis zur nächsten Veröffentlichung gibt es keine. */
    public Standardschreiben zurueckziehen() {
        return mit(versionen.zurueckziehen());
    }

    private Standardschreiben mit(Versionsfolge neueVersionen) {
        return new Standardschreiben(id, name, briefpapier, bezugstyp, neueVersionen);
    }

    /** Gleich sind zwei Instanzen mit derselben ID, auch in verschiedenen Ständen. */
    @Override
    public boolean equals(Object anderes) {
        return anderes instanceof Standardschreiben vorlage && id.equals(vorlage.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Standardschreiben[id=" + id.wert() + ", name=" + name + "]";
    }
}
