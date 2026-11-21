package dieterbaier.standardschreiben.domaene.modell;

/** Art des Briefpapiers. Bei geschäftlichen Briefen legt DIN 5008 die Form fest (Höhe des Anschriftfelds). */
public enum Briefpapiertyp {

    PRIVAT,

    GESCHAEFTLICH_FORM_A,

    GESCHAEFTLICH_FORM_B;

    /** Ohne ausdrückliche Wahl gilt geschäftlich Form B. */
    public static final Briefpapiertyp STANDARD = GESCHAEFTLICH_FORM_B;
}
