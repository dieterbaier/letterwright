package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.validierung.Pruefe;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Vorlage für den Inhalt eines Schreibens mit Platzhaltern. Ihr ist genau ein Briefpapier zugewiesen. Aggregate Root
 * über ihre {@link Vorlagenversion Versionen}, mit denselben Regeln wie das Briefpapier (siehe {@link Versionsfolge}).
 * Das Briefpapier ist ein eigenes Aggregat und wird nur über seine ID referenziert.
 */
@AggregateRoot
public final class Standardschreiben {

    @Identity
    private final VorlagenId id;
    private final String name;
    private final VorlagenId briefpapier;
    private final Versionsfolge versionen;

    private Standardschreiben(VorlagenId id, String name, VorlagenId briefpapier, Versionsfolge versionen) {
        this.id = Pruefe.pflicht(id, "Die ID des Standardschreibens");
        this.name = Pruefe.nichtBlank(name, "Der Name des Standardschreibens");
        this.briefpapier = Pruefe.pflicht(briefpapier, "Das Briefpapier des Standardschreibens");
        this.versionen = versionen;
    }

    /** Legt ein neues Standardschreiben mit der ersten Version als Entwurf an. */
    public static Standardschreiben anlegen(VorlagenId id, String name, VorlagenId briefpapier, String inhalt) {
        return new Standardschreiben(id, name, briefpapier, Versionsfolge.erste(
                Pruefe.pflicht(id, "Die ID des Standardschreibens"), inhalt));
    }

    /** Stellt ein gespeichertes Standardschreiben mit seinen Versionen wieder her und prüft deren Regeln. */
    public static Standardschreiben wiederherstellen(
            VorlagenId id, String name, VorlagenId briefpapier, List<Vorlagenversion> versionen) {
        return new Standardschreiben(id, name, briefpapier, Versionsfolge.wiederherstellen(
                Pruefe.pflicht(id, "Die ID des Standardschreibens"), versionen));
    }

    public VorlagenId id() {
        return id;
    }

    public String name() {
        return name;
    }

    /** Das zugewiesene Briefpapier. */
    public VorlagenId briefpapier() {
        return briefpapier;
    }

    /** Alle Versionen, aufsteigend nach Nummer. */
    public List<Vorlagenversion> versionen() {
        return versionen.alle();
    }

    public Optional<Vorlagenversion> version(Versionsnummer nummer) {
        return versionen.version(nummer);
    }

    /** Der Entwurf, falls es einen gibt. Es gibt höchstens einen, und er ist die neueste Version. */
    public Optional<Vorlagenversion> entwurf() {
        return versionen.entwurf();
    }

    /** Die veröffentlichte Version mit der höchsten Nummer, falls es eine gibt. */
    public Optional<Vorlagenversion> neuesteVeroeffentlichteVersion() {
        return versionen.neuesteVeroeffentlichte();
    }

    /** Ersetzt den Inhalt des Entwurfs. Leer ist erlaubt, fehlend nicht. */
    public Standardschreiben aendereEntwurf(String inhalt) {
        return mit(versionen.aendereEntwurf(inhalt));
    }

    /** Veröffentlicht den Entwurf. Er braucht Inhalt; danach ist er unveränderlich. */
    public Standardschreiben veroeffentlicheEntwurf() {
        return mit(versionen.veroeffentlicheEntwurf());
    }

    /** Legt die nächste Version als Entwurf an. Geht nur, solange es keinen Entwurf gibt. */
    public Standardschreiben neuerEntwurf(String inhalt) {
        return mit(versionen.neuerEntwurf(inhalt));
    }

    /** Zieht eine veröffentlichte Version zurück. */
    public Standardschreiben zurueckziehen(Versionsnummer nummer) {
        return mit(versionen.zurueckziehen(nummer));
    }

    private Standardschreiben mit(Versionsfolge neueVersionen) {
        return new Standardschreiben(id, name, briefpapier, neueVersionen);
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
