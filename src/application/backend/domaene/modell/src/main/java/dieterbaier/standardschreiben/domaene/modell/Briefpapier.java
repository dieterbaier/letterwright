package dieterbaier.standardschreiben.domaene.modell;

/**
 * Vorlage für das Seitenlayout eines Schreibens. Der Inhalt, auch die Stilvorgaben, steht in ihren
 * {@link Vorlagenversion Versionen}; wie die Pflege die Stilvorgaben strukturiert, legt #8 fest.
 */
public record Briefpapier(VorlagenId id, String name, Briefpapiertyp typ) {

    public Briefpapier {
        Pruefe.nichtLeer(name, "Der Name des Briefpapiers");
    }

    /** Legt ein Briefpapier mit dem Standardtyp (geschäftlich Form B) an. */
    public static Briefpapier mitStandardtyp(VorlagenId id, String name) {
        return new Briefpapier(id, name, Briefpapiertyp.STANDARD);
    }
}
