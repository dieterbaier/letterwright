package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.Entity;

/**
 * Ein Stand eines Briefpapiers oder Standardschreibens (ADR-007). Der Inhalt ist semantisches HTML mit CSS Paged Media
 * (ADR-003).
 *
 * <p>Entität im Aggregat ihrer Vorlage: Zustandswechsel gibt es nur über {@link Briefpapier} bzw.
 * {@link Standardschreiben}, die dabei die Regeln über alle Versionen hinweg sichern. Gleich sind zwei Instanzen, wenn
 * sie dieselbe Version derselben Vorlage meinen, auch in verschiedenen Zuständen.
 *
 * <p>Der Inhalt darf leer sein, solange die Version ein Entwurf ist. Veröffentlichen verlangt Inhalt.
 */
@Entity
public final class Vorlagenversion {

    private final VorlagenId vorlage;
    private final Versionsnummer nummer;
    private final Vorlagenzustand zustand;
    private final String inhalt;

    private Vorlagenversion(VorlagenId vorlage, Versionsnummer nummer, Vorlagenzustand zustand, String inhalt) {
        this.vorlage = Pruefe.pflicht(vorlage, "Die Vorlage der Version");
        this.nummer = Pruefe.pflicht(nummer, "Die Versionsnummer");
        this.zustand = Pruefe.pflicht(zustand, "Der Zustand der Version");
        this.inhalt = Pruefe.pflicht(inhalt, "Der Inhalt der Version");
        if (zustand != Vorlagenzustand.ENTWURF) {
            Pruefe.nichtBlank(inhalt, "Der Inhalt einer veröffentlichten oder zurückgezogenen Version");
        }
    }

    /** Stellt eine gespeicherte Version wieder her, etwa beim Laden aus der Persistenz. */
    public static Vorlagenversion wiederherstellen(
            VorlagenId vorlage, Versionsnummer nummer, Vorlagenzustand zustand, String inhalt) {
        return new Vorlagenversion(vorlage, nummer, zustand, inhalt);
    }

    static Vorlagenversion entwurf(VorlagenId vorlage, Versionsnummer nummer, String inhalt) {
        return new Vorlagenversion(vorlage, nummer, Vorlagenzustand.ENTWURF, inhalt);
    }

    Vorlagenversion mitInhalt(String neuerInhalt) {
        return new Vorlagenversion(vorlage, nummer, zustand, neuerInhalt);
    }

    Vorlagenversion imZustand(Vorlagenzustand neuerZustand) {
        return new Vorlagenversion(vorlage, nummer, neuerZustand, inhalt);
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

    /** Der Verweis auf diese Version, wie ihn Snapshot und Schreiben speichern. */
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
