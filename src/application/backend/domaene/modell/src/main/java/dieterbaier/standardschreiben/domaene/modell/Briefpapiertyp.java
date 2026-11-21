package dieterbaier.standardschreiben.domaene.modell;

import org.jmolecules.ddd.annotation.ValueObject;

/** Art des Briefpapiers. Bei geschäftlichen Briefen legt DIN 5008 die Form fest (Höhe des Anschriftfelds). */
@ValueObject
public enum Briefpapiertyp {

    PRIVAT,

    GESCHAEFTLICH_FORM_A,

    GESCHAEFTLICH_FORM_B;

    /** Ohne ausdrückliche Wahl gilt geschäftlich Form B. */
    public static final Briefpapiertyp STANDARD = GESCHAEFTLICH_FORM_B;
}
