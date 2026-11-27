/**
 * Java API for the host application, see ADR-023: use cases the host calls from its own code and in its own
 * transaction, and the data types that flow through them. Like the SPI, it depends on no other module of the library
 * and not on Spring (pattern from ADR-016). The callback on saving through the user interface is described by the SPI,
 * not by this package.
 *
 * <p>Today there is one use case: {@link dieterbaier.letterwright.api.SaveLetter}.
 *
 * <p>For all types of this package:
 * <ul>
 *   <li>No parameter and no return value is {@code null}. The package is {@code @NullMarked} (JSpecify); where a value
 *       may be absent, the type says so.</li>
 *   <li>All data types are immutable and validate themselves on creation. A missing or invalid value results in an
 *       {@link IllegalArgumentException} naming the attribute.</li>
 *   <li>Business rejections are cases of the result type, not exceptions. An exception is a technical error.</li>
 * </ul>
 */
@NullMarked
package dieterbaier.letterwright.api;

import org.jspecify.annotations.NullMarked;
