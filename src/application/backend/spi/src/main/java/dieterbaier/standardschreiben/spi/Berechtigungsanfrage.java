package dieterbaier.standardschreiben.spi;

/**
 * Anfrage an die {@link Berechtigungspruefung}. Es gibt genau drei Arten, und ein Host unterscheidet sie mit
 * {@code switch}:
 *
 * <pre>{@code
 * return switch (anfrage) {
 *     case Berechtigungsanfrage.ZuVorlagen vorlagen -> ...;
 *     case Berechtigungsanfrage.ZuSchreiben schreiben -> ... schreiben.bezug() ...;
 *     case Berechtigungsanfrage.ZumErzeugen erzeugen -> ... erzeugen.bezug(), erzeugen.standardschreiben() ...;
 * };
 * }</pre>
 *
 * <p>Zum Erzeugen eines Schreibens kennt der Host das Standardschreiben. So kann er je Einstiegspunkt festlegen, welche
 * Standardschreiben zu welchem Bezug zulässig sind. Dass der Bezugstyp zum Standardschreiben passt, hat die Bibliothek
 * vorher schon geprüft.
 */
public sealed interface Berechtigungsanfrage {

    /** Der angemeldete Benutzer, wie ihn die Spring Security des Hosts kennt. */
    String benutzerkennung();

    /** Darf der Benutzer diese Handlung an Briefpapier und Standardschreiben ausführen? */
    static Berechtigungsanfrage zuVorlagen(String benutzerkennung, VorlagenAktion aktion) {
        return new ZuVorlagen(benutzerkennung, aktion);
    }

    /** Darf der Benutzer diese Handlung an den gespeicherten Schreiben zu diesem fachlichen Bezug ausführen? */
    static Berechtigungsanfrage zuSchreiben(String benutzerkennung, SchreibenAktion aktion, FachlicherBezug bezug) {
        return new ZuSchreiben(benutzerkennung, aktion, bezug);
    }

    /**
     * Darf der Benutzer aus diesem Standardschreiben zu diesem fachlichen Bezug ein Schreiben erzeugen? Gefragt wird beim
     * Erzeugen der Vorschau und erneut beim Speichern.
     */
    static Berechtigungsanfrage zumErzeugen(
            String benutzerkennung, FachlicherBezug bezug, Standardschreibenkennung standardschreiben) {
        return new ZumErzeugen(benutzerkennung, bezug, standardschreiben);
    }

    /** Siehe {@link #zuVorlagen(String, VorlagenAktion)}. */
    record ZuVorlagen(String benutzerkennung, VorlagenAktion aktion) implements Berechtigungsanfrage {

        public ZuVorlagen {
            Pflicht.text(benutzerkennung, "Die Benutzerkennung");
            Pflicht.wert(aktion, "Die Aktion");
        }
    }

    /** Siehe {@link #zuSchreiben(String, SchreibenAktion, FachlicherBezug)}. */
    record ZuSchreiben(String benutzerkennung, SchreibenAktion aktion, FachlicherBezug bezug)
            implements Berechtigungsanfrage {

        public ZuSchreiben {
            Pflicht.text(benutzerkennung, "Die Benutzerkennung");
            Pflicht.wert(aktion, "Die Aktion");
            Pflicht.wert(bezug, "Der fachliche Bezug");
        }
    }

    /** Siehe {@link #zumErzeugen(String, FachlicherBezug, Standardschreibenkennung)}. */
    record ZumErzeugen(String benutzerkennung, FachlicherBezug bezug, Standardschreibenkennung standardschreiben)
            implements Berechtigungsanfrage {

        public ZumErzeugen {
            Pflicht.text(benutzerkennung, "Die Benutzerkennung");
            Pflicht.wert(bezug, "Der fachliche Bezug");
            Pflicht.wert(standardschreiben, "Das Standardschreiben");
        }
    }
}
