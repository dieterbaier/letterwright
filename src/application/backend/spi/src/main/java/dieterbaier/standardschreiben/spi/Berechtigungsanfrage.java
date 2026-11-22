package dieterbaier.standardschreiben.spi;

/**
 * Anfrage an die {@link Berechtigungspruefung}. Es gibt genau zwei Arten, und ein Host unterscheidet sie mit
 * {@code switch}:
 *
 * <pre>{@code
 * return switch (anfrage) {
 *     case Berechtigungsanfrage.ZuVorlagen vorlagen -> ...;
 *     case Berechtigungsanfrage.ZuSchreiben schreiben -> ... schreiben.bezug() ...;
 * };
 * }</pre>
 */
public sealed interface Berechtigungsanfrage {

    /** Der angemeldete Benutzer, wie ihn die Spring Security des Hosts kennt. */
    String benutzerkennung();

    /** Die Handlung, für die der Benutzer die Berechtigung braucht. */
    Aktion aktion();

    /** Darf der Benutzer diese Handlung an Briefpapier und Standardschreiben ausführen? */
    static Berechtigungsanfrage zuVorlagen(String benutzerkennung, VorlagenAktion aktion) {
        return new ZuVorlagen(benutzerkennung, aktion);
    }

    /** Darf der Benutzer diese Handlung an den Schreiben zu diesem fachlichen Bezug ausführen? */
    static Berechtigungsanfrage zuSchreiben(String benutzerkennung, SchreibenAktion aktion, FachlicherBezug bezug) {
        return new ZuSchreiben(benutzerkennung, aktion, bezug);
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
}
