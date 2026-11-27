package dieterbaier.letterwright.domain.model.template;

import org.jmolecules.ddd.annotation.ValueObject;

/** Kind of letterhead. For business letters DIN 5008 defines the form (height of the address field). */
@ValueObject
public enum LetterheadType {

    PRIVATE,

    BUSINESS_FORM_A,

    BUSINESS_FORM_B;

    /** Without an explicit choice, business form B applies. */
    public static final LetterheadType STANDARD = BUSINESS_FORM_B;
}
