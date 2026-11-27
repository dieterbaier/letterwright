package dieterbaier.letterwright.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * An action on the stored letters of a {@link BusinessReference}. Creating is not one of them because the host also
 * needs to know the letter template for it (ADR-020).
 */
@ValueObject
public enum LetterAction {

    VIEW,

    DOWNLOAD,

    /** How deletion happens is decided by the host's deletion policy. */
    DELETE
}
