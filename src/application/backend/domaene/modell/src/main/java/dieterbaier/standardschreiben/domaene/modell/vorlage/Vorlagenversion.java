package dieterbaier.standardschreiben.domaene.modell.vorlage;

import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

/**
 * Ein Stand eines Briefpapiers oder Standardschreibens (ADR-007). Der Inhalt ist semantisches HTML mit CSS Paged Media
 * (ADR-003).
 *
 * <p>Entität im Aggregat ihrer Vorlage: Zustandswechsel gibt es nur über {@link Briefpapier} bzw.
 * {@link Standardschreiben}, die dabei die Regeln über alle Versionen hinweg sichern. Gleich sind zwei Instanzen, wenn
 * sie dieselbe Version derselben Vorlage meinen, auch in verschiedenen Zuständen.
 *
 * <p>Der Inhalt darf leer sein, solange die Version ein Entwurf ist. Veröffentlichen verlangt Inhalt.
 *
 * <p>Die Beispieldaten füllen die Platzhalter im Probe-PDF, das vor dem Veröffentlichen geprüft wird. Sie gehören zur
 * Version, damit nachvollziehbar bleibt, womit eine veröffentlichte Version geprüft wurde (ADR-022).
 */
@Entity
public final class Vorlagenversion {

    private final VorlagenId vorlage;
    private final Versionsnummer nummer;
    private final Vorlagenzustand zustand;
    private final String inhalt;
    private final Datenkontext beispieldaten;

    private Vorlagenversion(VorlagenId vorlage, Versionsnummer nummer, Vorlagenzustand zustand, String inhalt,
            Datenkontext beispieldaten) {
        this.vorlage = Pruefe.pflicht(vorlage, "Die Vorlage der Version");
        this.nummer = Pruefe.pflicht(nummer, "Die Versionsnummer");
        this.zustand = Pruefe.pflicht(zustand, "Der Zustand der Version");
        this.inhalt = Pruefe.pflicht(inhalt, "Der Inhalt der Version");
        this.beispieldaten = Pruefe.pflicht(beispieldaten, "Die Beispieldaten der Version");
        if (zustand != Vorlagenzustand.ENTWURF) {
            Pruefe.nichtBlank(inhalt, "Der Inhalt einer freigegebenen Version");
        }
    }

    public static Vorlagenversion wiederherstellen(VorlagenId vorlage, Versionsnummer nummer,
            Vorlagenzustand zustand, String inhalt, Datenkontext beispieldaten) {
        return new Vorlagenversion(vorlage, nummer, zustand, inhalt, beispieldaten);
    }

    static Vorlagenversion entwurf(VorlagenId vorlage, Versionsnummer nummer, String inhalt,
            Datenkontext beispieldaten) {
        return new Vorlagenversion(vorlage, nummer, Vorlagenzustand.ENTWURF, inhalt, beispieldaten);
    }

    Vorlagenversion mitInhalt(String neuerInhalt, Datenkontext neueBeispieldaten) {
        return new Vorlagenversion(vorlage, nummer, zustand, neuerInhalt, neueBeispieldaten);
    }

    Vorlagenversion imZustand(Vorlagenzustand neuerZustand) {
        return new Vorlagenversion(vorlage, nummer, neuerZustand, inhalt, beispieldaten);
    }

    public VorlagenId vorlage() {
        return vorlage;
    }

    public Versionsnummer nummer() {
        return nummer;
    }

    public Vorlagenzustand zustand() {
        return zustand;
    }

    public String inhalt() {
        return inhalt;
    }

    public Datenkontext beispieldaten() {
        return beispieldaten;
    }

    /** Ein Entwurf mit Inhalt, der nicht nur aus Leerraum besteht. */
    public boolean istVeroeffentlichbar() {
        return zustand == Vorlagenzustand.ENTWURF && !inhalt.isBlank();
    }

    /**
     * Die Identität der Version: Vorlage und Nummer. Snapshot und Schreiben speichern genau diesen Verweis.
     */
    @Identity
    public Versionsverweis verweis() {
        return new Versionsverweis(vorlage, nummer);
    }

    @Override
    public boolean equals(Object anderes) {
        return anderes instanceof Vorlagenversion version && verweis().equals(version.verweis());
    }

    @Override
    public int hashCode() {
        return verweis().hashCode();
    }

    @Override
    public String toString() {
        return "Vorlagenversion[vorlage=" + vorlage.wert() + ", nummer=" + nummer.wert() + ", zustand=" + zustand + "]";
    }
}
