package dieterbaier.letterwright.api;

/**
 * Das Domänenobjekt des Hosts, zu dem das Schreiben erzeugt wurde, als Typ und ID (z. B. {@code Kunde}, {@code 4711}).
 * Es sind genau die Werte, die der Host beim Erzeugen der Vorschau übergeben hat; für sie gilt der Vertrag von
 * {@code spi.BusinessReference}. Die API hat einen eigenen Typ, weil sie nicht vom SPI abhängt.
 *
 * @param type der Bezugstyp des Hosts, z. B. {@code Kunde}; nicht leer und nicht nur aus Leerraum
 * @param id die ID des Objekts; nicht leer und nicht nur aus Leerraum
 */
public record BusinessReference(String type, String id) {

    public BusinessReference {
        Require.text(type, "Der Typ des fachlichen Bezugs");
        Require.text(id, "Die ID des fachlichen Bezugs");
    }
}
