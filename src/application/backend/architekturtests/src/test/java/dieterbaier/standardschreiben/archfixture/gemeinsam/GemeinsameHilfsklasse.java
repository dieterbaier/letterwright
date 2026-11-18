package dieterbaier.standardschreiben.archfixture.gemeinsam;

/** Fixture: Klasse in einem Paket, das keine Regel namentlich nennt. */
public final class GemeinsameHilfsklasse {

    private GemeinsameHilfsklasse() {
    }

    public static String normalisiere(String wert) {
        return wert.strip();
    }
}
