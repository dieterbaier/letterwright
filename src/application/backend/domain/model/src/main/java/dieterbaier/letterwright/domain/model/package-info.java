/**
 * Domain model of letterwright. Free of frameworks, see ADR-002. Knows no domain services (ADR-015). The identifiers
 * follow the glossary (arc42 chapter 12).
 *
 * <p>There is one subpackage per aggregate: {@code template} (letterheads and letter templates), {@code snapshot} and
 * {@code letter}. What only one aggregate needs lives in its package and is encapsulated there as far as possible.
 * This package holds the value types shared by several aggregates and the ports; it knows none of the subpackages.
 *
 * <p>All types are immutable. State changes return new instances.
 */
@NullMarked
package dieterbaier.letterwright.domain.model;

import org.jspecify.annotations.NullMarked;
