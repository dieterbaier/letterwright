package dieterbaier.standardschreiben.spi;

/**
 * Anfrage an den {@link PlatzhalterdatenAnbieter}: Welche Werte braucht dieses Standardschreiben für diesen fachlichen
 * Bezug?
 *
 * @param benutzerkennung der angemeldete Benutzer, wie ihn die Spring Security des Hosts kennt
 */
public record Datenanfrage(String benutzerkennung, FachlicherBezug bezug, Standardschreibenkennung standardschreiben) {

    public Datenanfrage {
        Pflicht.text(benutzerkennung, "Die Benutzerkennung");
        Pflicht.wert(bezug, "Der fachliche Bezug");
        Pflicht.wert(standardschreiben, "Das Standardschreiben");
    }
}
