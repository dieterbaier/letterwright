package dieterbaier.standardschreiben.hostvertrag;

import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Berechtigungspruefung;
import dieterbaier.standardschreiben.spi.Datenanfrage;
import dieterbaier.standardschreiben.spi.Datenkontext;
import dieterbaier.standardschreiben.spi.GespeichertesSchreiben;
import dieterbaier.standardschreiben.spi.Loeschanfrage;
import dieterbaier.standardschreiben.spi.Loeschentscheidung;
import dieterbaier.standardschreiben.spi.Loeschrichtlinie;
import dieterbaier.standardschreiben.spi.NachDemSpeichern;
import dieterbaier.standardschreiben.spi.PlatzhalterdatenAnbieter;
import dieterbaier.standardschreiben.spi.Rueckrufergebnis;
import java.util.Map;

/**
 * Ein Host, der nur den Starter einbindet und alle drei SPI-Schnittstellen implementiert, dazu den optionalen Rückruf
 * nach dem Speichern. Dass diese Klasse kompiliert, ist der positive Teil des Nachweises.
 */
final class BeispielHost implements PlatzhalterdatenAnbieter, Berechtigungspruefung, Loeschrichtlinie, NachDemSpeichern {

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

    /** Ein Host, der zu jedem Kunden höchstens ein Schreiben zulässt, etwa weil die Aufgabe damit erledigt ist. */
    @Override
    public Rueckrufergebnis angelegt(GespeichertesSchreiben schreiben) {
        return schreiben.bezug().id().equals("4711")
                ? Rueckrufergebnis.abgelehnt("Die Aufgabe zum Kunden 4711 ist bereits erledigt")
                : Rueckrufergebnis.angenommen();
    }
}
