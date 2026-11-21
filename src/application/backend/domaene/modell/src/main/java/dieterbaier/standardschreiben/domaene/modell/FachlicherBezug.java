package dieterbaier.standardschreiben.domaene.modell;

/**
 * Das Domänenobjekt der Host-Anwendung, zu dem ein Schreiben erzeugt wird, als Typ und ID (z. B. Kunde 4711). Die
 * Bibliothek deutet beides nicht, sie gibt es nur an den Host zurück.
 */
public record FachlicherBezug(String typ, String id) {

    public FachlicherBezug {
        Pruefe.nichtLeer(typ, "Der Typ des fachlichen Bezugs");
        Pruefe.nichtLeer(id, "Die ID des fachlichen Bezugs");
    }
}
