package dieterbaier.standardschreiben.api;

/**
 * Das Domänenobjekt des Hosts, zu dem das Schreiben erzeugt wurde, als Typ und ID (z. B. {@code Kunde}, {@code 4711}).
 * Es sind genau die Werte, die der Host beim Erzeugen der Vorschau übergeben hat; für sie gilt der Vertrag von
 * {@code spi.FachlicherBezug}. Die API hat einen eigenen Typ, weil sie nicht vom SPI abhängt.
 *
 * @param typ der Bezugstyp des Hosts, z. B. {@code Kunde}; nicht leer und nicht nur aus Leerraum
 * @param id die ID des Objekts; nicht leer und nicht nur aus Leerraum
 */
public record FachlicherBezug(String typ, String id) {

    public FachlicherBezug {
        Pflicht.text(typ, "Der Typ des fachlichen Bezugs");
        Pflicht.text(id, "Die ID des fachlichen Bezugs");
    }
}
