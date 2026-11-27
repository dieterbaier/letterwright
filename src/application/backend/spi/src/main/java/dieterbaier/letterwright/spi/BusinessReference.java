package dieterbaier.letterwright.spi;

/**
 * The host's domain object for which a letter is created, as type and ID (e.g. {@code Customer}, {@code 4711}). The
 * library does not interpret either; it only passes them on.
 *
 * <p>Type and ID together identify exactly one object of the host. If the host needs several keys, it encodes them in
 * the ID itself and decodes them again. The library compares references by equality and stores them with every
 * letter. The ID must therefore be
 *
 * <ul>
 *   <li>unique: different objects of the same reference type get different IDs. An encoding of several keys must
 *       therefore be free of collisions and unambiguously decodable,
 *   <li>canonical: the same object always yields the same string,
 *   <li>permanently stable: no business values that can change,
 *   <li>safe to expose: it comes from the browser, the host checks it and reveals no confidential data in it.
 * </ul>
 *
 * <p>Before the host encodes several keys in one ID, it checks whether the reference is cut correctly: often a
 * separate reference type with its own ID fits better.
 *
 * @param type the host's reference type, e.g. {@code Customer}; not empty and not only whitespace
 * @param id the ID of the object according to the contract above; not empty and not only whitespace
 */
public record BusinessReference(String type, String id) {

    public BusinessReference {
        Require.text(type, "The type of the business reference");
        Require.text(id, "The ID of the business reference");
    }
}
