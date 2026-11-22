package dieterbaier.standardschreiben.hostvertrag;

import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Berechtigungspruefung;
import dieterbaier.standardschreiben.spi.Datenanfrage;
import dieterbaier.standardschreiben.spi.Datenkontext;
import dieterbaier.standardschreiben.spi.Loeschanfrage;
import dieterbaier.standardschreiben.spi.Loeschentscheidung;
import dieterbaier.standardschreiben.spi.Loeschrichtlinie;
import dieterbaier.standardschreiben.spi.PlatzhalterdatenAnbieter;
import java.util.Map;

/**
 * Ein Host, der nur den Starter einbindet und alle drei SPI-Schnittstellen implementiert. Dass diese Klasse kompiliert,
 * ist der positive Teil des Nachweises.
 */
final class BeispielHost implements PlatzhalterdatenAnbieter, Berechtigungspruefung, Loeschrichtlinie {

    @Override
    public Datenkontext datenkontext(Datenanfrage anfrage) {
        return new Datenkontext(Map.of("kunde", anfrage.bezug().id()));
    }

    @Override
    public boolean istErlaubt(Berechtigungsanfrage anfrage) {
        return switch (anfrage) {
            case Berechtigungsanfrage.ZuVorlagen vorlagen -> false;
            case Berechtigungsanfrage.ZuSchreiben schreiben -> schreiben.benutzerkennung().equals("erika");
            case Berechtigungsanfrage.ZumErzeugen erzeugen -> erzeugen.benutzerkennung().equals("erika")
                    && erzeugen.standardschreiben().name().equals("Mahnung");
        };
    }

    @Override
    public Loeschentscheidung entscheide(Loeschanfrage anfrage) {
        return Loeschentscheidung.logisch();
    }
}
