package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Das Domänenobjekt der Host-Anwendung, zu dem ein Schreiben erzeugt wird, als Typ und ID (z. B. Kunde 4711). Die
 * Bibliothek deutet beides nicht, sie gibt es nur an den Host zurück.
 */
@ValueObject
public record FachlicherBezug(String typ, String id) {

    public FachlicherBezug {
        Pruefe.nichtBlank(typ, "Der Typ des fachlichen Bezugs");
        Pruefe.nichtBlank(id, "Die ID des fachlichen Bezugs");
    }
}
