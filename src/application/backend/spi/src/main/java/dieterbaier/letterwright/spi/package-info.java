/**
 * SPI towards the host application: interfaces the host implements and the data types that flow through them, see
 * ADR-016. Depends on no other module of the library and not on Spring.
 *
 * <p>The host implements three interfaces: {@link dieterbaier.letterwright.spi.PlaceholderDataProvider},
 * {@link dieterbaier.letterwright.spi.AuthorizationCheck} (optional, the starter ships a default implementation) and
 * {@link dieterbaier.letterwright.spi.DeletionPolicy}. Optionally, the callback
 * {@link dieterbaier.letterwright.spi.LetterSaveCallback} is added (ADR-023); it does not count among these three.
 *
 * <p>For all types of this package:
 * <ul>
 *   <li>No parameter and no return value is {@code null}. The package is {@code @NullMarked} (JSpecify); where a value
 *       may be absent, the type says so. If an implementation of the host returns {@code null}, the library aborts
 *       with a host contract violation.</li>
 *   <li>All data types are immutable and validate themselves on creation. A missing or invalid value results in an
 *       {@link IllegalArgumentException} naming the attribute.</li>
 * </ul>
 */
@NullMarked
package dieterbaier.letterwright.spi;

import org.jspecify.annotations.NullMarked;
