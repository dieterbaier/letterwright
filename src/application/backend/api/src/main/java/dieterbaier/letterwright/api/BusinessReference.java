package dieterbaier.letterwright.api;

/**
 * The host's domain object for which the letter was created, as type and ID (e.g. {@code Customer}, {@code 4711}).
 * These are exactly the values the host passed when the preview was created; the contract of
 * {@code spi.BusinessReference} applies to them. The API has its own type because it does not depend on the SPI.
 *
 * @param type the host's reference type, e.g. {@code Customer}; not empty and not only whitespace
 * @param id the ID of the object; not empty and not only whitespace
 */
public record BusinessReference(String type, String id) {

    public BusinessReference {
        Require.text(type, "The type of the business reference");
        Require.text(id, "The ID of the business reference");
    }
}
