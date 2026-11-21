package dieterbaier.standardschreiben.domaene.modell;

/** Laufende Nummer einer Vorlagenversion, beginnend bei 1. */
public record Versionsnummer(int wert) {

    /** Die Nummer der ersten Version einer Vorlage. */
    public static final Versionsnummer ERSTE = new Versionsnummer(1);

    public Versionsnummer {
        if (wert < 1) {
            throw new IllegalArgumentException("Eine Versionsnummer beginnt bei 1, war " + wert);
        }
    }

    /** Die Nummer der nächsten Version. */
    public Versionsnummer naechste() {
        return new Versionsnummer(Math.addExact(wert, 1));
    }
}
