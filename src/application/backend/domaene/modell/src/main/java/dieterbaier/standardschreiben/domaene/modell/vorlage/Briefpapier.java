package dieterbaier.standardschreiben.domaene.modell.vorlage;

import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.validierung.Pruefe;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Vorlage für das Seitenlayout eines Schreibens. Aggregate Root über seine {@link Vorlagenversion Versionen}: Jede
 * Änderung an einer Version geht über das Briefpapier, das die Regeln über alle Versionen hinweg sichert (siehe
 * {@link Versionsfolge}).
 *
 * <p>Der Inhalt, auch die Stilvorgaben, steht in den Versionen; wie die Pflege die Stilvorgaben strukturiert, legt #8
 * fest. Jeder Übergang liefert eine neue Instanz.
 */
@AggregateRoot
public final class Briefpapier {

    @Identity
    private final VorlagenId id;
    private final String name;
    private final Briefpapiertyp typ;
    private final Versionsfolge versionen;

    private Briefpapier(VorlagenId id, String name, Briefpapiertyp typ, Versionsfolge versionen) {
        this.id = Pruefe.pflicht(id, "Die ID des Briefpapiers");
        this.name = Pruefe.nichtBlank(name, "Der Name des Briefpapiers");
        this.typ = Pruefe.pflicht(typ, "Der Typ des Briefpapiers");
        this.versionen = versionen;
    }

    /** Legt ein neues Briefpapier mit der ersten Version als Entwurf an. */
    public static Briefpapier anlegen(VorlagenId id, String name, Briefpapiertyp typ, String inhalt) {
        return new Briefpapier(id, name, typ, Versionsfolge.erste(Pruefe.pflicht(id, "Die ID des Briefpapiers"),
                inhalt));
    }

    /** Legt ein neues Briefpapier mit dem Standardtyp (geschäftlich Form B) an. */
    public static Briefpapier anlegenMitStandardtyp(VorlagenId id, String name, String inhalt) {
        return anlegen(id, name, Briefpapiertyp.STANDARD, inhalt);
    }

    /** Stellt ein gespeichertes Briefpapier mit seinen Versionen wieder her und prüft deren Regeln. */
    public static Briefpapier wiederherstellen(
            VorlagenId id, String name, Briefpapiertyp typ, List<Vorlagenversion> versionen) {
        return new Briefpapier(id, name, typ, Versionsfolge.wiederherstellen(
                Pruefe.pflicht(id, "Die ID des Briefpapiers"), versionen));
    }

    public VorlagenId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Briefpapiertyp typ() {
        return typ;
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
    public Briefpapier aendereEntwurf(String inhalt) {
        return mit(versionen.aendereEntwurf(inhalt));
    }

    /** Ersetzt Inhalt und Beispieldaten des Entwurfs. */
    public Briefpapier aendereEntwurf(String inhalt, Datenkontext beispieldaten) {
        return mit(versionen.aendereEntwurf(inhalt, beispieldaten));
    }

    /**
     * Veröffentlicht den Entwurf. Er braucht Inhalt; danach ist er unveränderlich. Die bisher veröffentlichte Version ist
     * danach abgelöst (ADR-022).
     */
    public Briefpapier veroeffentlicheEntwurf() {
        return mit(versionen.veroeffentlicheEntwurf());
    }

    /**
     * Legt die nächste Version als Entwurf an und übernimmt die Beispieldaten der neuesten. Geht nur, solange es keinen
     * Entwurf gibt.
     */
    public Briefpapier neuerEntwurf(String inhalt) {
        return mit(versionen.neuerEntwurf(inhalt));
    }

    /** Nimmt die veröffentlichte Version ohne Nachfolger zurück; bis zur nächsten Veröffentlichung gibt es keine. */
    public Briefpapier zurueckziehen() {
        return mit(versionen.zurueckziehen());
    }

    private Briefpapier mit(Versionsfolge neueVersionen) {
        return new Briefpapier(id, name, typ, neueVersionen);
    }

    /** Gleich sind zwei Instanzen mit derselben ID, auch in verschiedenen Ständen. */
    @Override
    public boolean equals(Object anderes) {
        return anderes instanceof Briefpapier vorlage && id.equals(vorlage.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Briefpapier[id=" + id.wert() + ", name=" + name + "]";
    }
}
