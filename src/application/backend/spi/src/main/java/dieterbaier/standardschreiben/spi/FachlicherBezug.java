package dieterbaier.standardschreiben.spi;

/**
 * Das Domänenobjekt des Hosts, zu dem ein Schreiben erzeugt wird, als Typ und ID (z. B. {@code Kunde}, {@code 4711}).
 * Die Bibliothek deutet beides nicht, sie reicht es nur weiter.
 */
public record FachlicherBezug(String typ, String id) {

    public FachlicherBezug {
        Pflicht.text(typ, "Der Typ des fachlichen Bezugs");
        Pflicht.text(id, "Die ID des fachlichen Bezugs");
    }
}
