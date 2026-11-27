package dieterbaier.letterwright.domain.model.template;

import org.jmolecules.ddd.annotation.ValueObject;

/** Art des Briefpapiers. Bei geschäftlichen Briefen legt DIN 5008 die Form fest (Höhe des Anschriftfelds). */
@ValueObject
public enum LetterheadType {

    PRIVATE,

    BUSINESS_FORM_A,

    BUSINESS_FORM_B;

    /** Ohne ausdrückliche Wahl gilt geschäftlich Form B. */
    public static final LetterheadType STANDARD = BUSINESS_FORM_B;
}
