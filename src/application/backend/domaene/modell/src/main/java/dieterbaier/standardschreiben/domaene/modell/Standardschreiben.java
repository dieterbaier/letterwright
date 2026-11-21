package dieterbaier.standardschreiben.domaene.modell;

/**
 * Vorlage für den Inhalt eines Schreibens mit Platzhaltern. Ihr ist genau ein Briefpapier zugewiesen. Der Inhalt steht
 * in ihren {@link Vorlagenversion Versionen}.
 */
public record Standardschreiben(VorlagenId id, String name, VorlagenId briefpapier) {

    public Standardschreiben {
        Pruefe.nichtLeer(name, "Der Name des Standardschreibens");
    }
}
