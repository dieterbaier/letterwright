/**
 * General checks with which self-validating types check their attributes in the constructor. Which rule applies to an
 * attribute is decided by the type itself; business rules such as state transitions do not belong here.
 *
 * <p>Error contract: every violated check throws a {@link dieterbaier.letterwright.validation.ValidationException}
 * with the label of the attribute, never a {@link NullPointerException}. Free of frameworks and without dependencies on
 * other modules. The SPI does not use this module (ADR-016).
 */
@NullMarked
package dieterbaier.letterwright.validation;

import org.jspecify.annotations.NullMarked;
