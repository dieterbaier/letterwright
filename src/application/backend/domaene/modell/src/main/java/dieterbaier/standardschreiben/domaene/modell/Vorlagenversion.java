package dieterbaier.standardschreiben.domaene.modell;

/**
 * Ein Stand eines Briefpapiers oder Standardschreibens (ADR-007). Der Inhalt ist semantisches HTML mit CSS Paged Media
 * (ADR-003).
 *
 * <p>Erlaubte Zustandswechsel: Entwurf → veröffentlicht → zurückgezogen. Nur ein Entwurf lässt sich ändern. Eine
 * veröffentlichte Version bleibt unverändert; wer sie ändern will, legt mit {@link #neuerEntwurf(String)} die nächste
 * Version an. Dass jede Nummer je Vorlage nur einmal vergeben wird, sichern Anwendungsfall und Persistenz, nicht diese
 * Klasse.
 *
 * <p>Jeder Übergang liefert eine neue Instanz; eine Instanz selbst ändert sich nie.
 */
public record Vorlagenversion(VorlagenId vorlage, Versionsnummer nummer, Vorlagenzustand zustand, String inhalt) {

    /** Legt die erste Version einer Vorlage als Entwurf an. */
    public static Vorlagenversion ersterEntwurf(VorlagenId vorlage, String inhalt) {
        return new Vorlagenversion(vorlage, Versionsnummer.ERSTE, Vorlagenzustand.ENTWURF, inhalt);
    }

    /** Der Verweis auf diese Version, wie ihn Snapshot und Schreiben speichern. */
    public Versionsverweis verweis() {
        return new Versionsverweis(vorlage, nummer);
    }

    /** Ersetzt den Inhalt eines Entwurfs. */
    public Vorlagenversion mitInhalt(String neuerInhalt) {
        verlange(Vorlagenzustand.ENTWURF, "Ändern");
        return new Vorlagenversion(vorlage, nummer, zustand, neuerInhalt);
    }

    /** Gibt einen Entwurf frei. */
    public Vorlagenversion veroeffentlichen() {
        verlange(Vorlagenzustand.ENTWURF, "Veröffentlichen");
        return new Vorlagenversion(vorlage, nummer, Vorlagenzustand.VEROEFFENTLICHT, inhalt);
    }

    /** Zieht eine veröffentlichte Version zurück. */
    public Vorlagenversion zurueckziehen() {
        verlange(Vorlagenzustand.VEROEFFENTLICHT, "Zurückziehen");
        return new Vorlagenversion(vorlage, nummer, Vorlagenzustand.ZURUECKGEZOGEN, inhalt);
    }

    /** Legt auf Grundlage dieser veröffentlichten Version die nächste Version als Entwurf an. */
    public Vorlagenversion neuerEntwurf(String neuerInhalt) {
        verlange(Vorlagenzustand.VEROEFFENTLICHT, "Einen neuen Entwurf anlegen");
        return new Vorlagenversion(vorlage, nummer.naechste(), Vorlagenzustand.ENTWURF, neuerInhalt);
    }

    private void verlange(Vorlagenzustand erwartet, String vorgang) {
        if (zustand != erwartet) {
            throw new UnzulaessigerZustandswechsel(verweis(), zustand, vorgang);
        }
    }
}
