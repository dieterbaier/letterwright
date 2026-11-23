package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Das Domänenobjekt der Host-Anwendung, zu dem ein Schreiben erzeugt wird, als Typ und ID (z. B. Kunde 4711). Die
 * Bibliothek deutet beides nicht, sie gibt es nur an den Host zurück.
 *
 * <p>Die Bibliothek vergleicht Bezüge trotzdem per Gleichheit: in der Übersicht, beim Löschen und bei den
 * Berechtigungen. Außerdem speichert sie den Bezug dauerhaft mit jedem Schreiben. Das trägt nur, weil der Host die ID
 * kanonisch und dauerhaft stabil vergibt und mehrere Schlüssel bei Bedarf selbst darin kodiert; den Vertrag nennt
 * {@code spi.FachlicherBezug}. Einen strukturierten Schlüssel gibt es deshalb bewusst nicht.
 */
@ValueObject
public record FachlicherBezug(String typ, String id) {

    public FachlicherBezug {
        Pruefe.nichtBlank(typ, "Der Typ des fachlichen Bezugs");
        Pruefe.nichtBlank(id, "Die ID des fachlichen Bezugs");
    }
}
