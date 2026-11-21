package dieterbaier.standardschreiben.domaene.modell;

/**
 * Kennung des angemeldeten Benutzers, wie sie die Host-Anwendung liefert. Die Bibliothek verwaltet keine Benutzer, sie
 * vergleicht nur Kennungen.
 */
public record BenutzerKennung(String wert) {

    public BenutzerKennung {
        Pruefe.nichtLeer(wert, "Die Benutzerkennung");
    }
}
