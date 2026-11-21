package dieterbaier.standardschreiben.adapter.out.host;

import dieterbaier.standardschreiben.domaene.modell.Aktion;
import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Loeschentscheidung;
import dieterbaier.standardschreiben.domaene.modell.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.Standardschreiben;
import dieterbaier.standardschreiben.spi.Datenanfrage;
import dieterbaier.standardschreiben.spi.Loeschanfrage;
import dieterbaier.standardschreiben.spi.Standardschreibenkennung;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;

/**
 * Bildet zwischen den Typen des SPI und denen des Domänenmodells ab, in beide Richtungen (ADR-016). Die Typen tragen
 * dieselben Fachbegriffe; die SPI-Typen sind hier voll qualifiziert, die Domänentypen importiert.
 */
final class Abbildung {

    private Abbildung() {
    }

    static dieterbaier.standardschreiben.spi.FachlicherBezug zumSpi(FachlicherBezug bezug) {
        return new dieterbaier.standardschreiben.spi.FachlicherBezug(bezug.typ(), bezug.id());
    }

    static FachlicherBezug zurDomaene(dieterbaier.standardschreiben.spi.FachlicherBezug bezug) {
        return new FachlicherBezug(bezug.typ(), bezug.id());
    }

    static dieterbaier.standardschreiben.spi.Aktion zumSpi(Aktion aktion) {
        return switch (aktion) {
            case VORLAGEN_BEARBEITEN -> dieterbaier.standardschreiben.spi.Aktion.VORLAGEN_BEARBEITEN;
            case VORLAGEN_VEROEFFENTLICHEN -> dieterbaier.standardschreiben.spi.Aktion.VORLAGEN_VEROEFFENTLICHEN;
            case SCHREIBEN_ERZEUGEN -> dieterbaier.standardschreiben.spi.Aktion.SCHREIBEN_ERZEUGEN;
            case SCHREIBEN_ANSEHEN -> dieterbaier.standardschreiben.spi.Aktion.SCHREIBEN_ANSEHEN;
            case SCHREIBEN_HERUNTERLADEN -> dieterbaier.standardschreiben.spi.Aktion.SCHREIBEN_HERUNTERLADEN;
            case SCHREIBEN_LOESCHEN -> dieterbaier.standardschreiben.spi.Aktion.SCHREIBEN_LOESCHEN;
        };
    }

    static Aktion zurDomaene(dieterbaier.standardschreiben.spi.Aktion aktion) {
        return switch (aktion) {
            case VORLAGEN_BEARBEITEN -> Aktion.VORLAGEN_BEARBEITEN;
            case VORLAGEN_VEROEFFENTLICHEN -> Aktion.VORLAGEN_VEROEFFENTLICHEN;
            case SCHREIBEN_ERZEUGEN -> Aktion.SCHREIBEN_ERZEUGEN;
            case SCHREIBEN_ANSEHEN -> Aktion.SCHREIBEN_ANSEHEN;
            case SCHREIBEN_HERUNTERLADEN -> Aktion.SCHREIBEN_HERUNTERLADEN;
            case SCHREIBEN_LOESCHEN -> Aktion.SCHREIBEN_LOESCHEN;
        };
    }

    static dieterbaier.standardschreiben.spi.Loeschentscheidung zumSpi(Loeschentscheidung entscheidung) {
        return switch (entscheidung) {
            case HART -> dieterbaier.standardschreiben.spi.Loeschentscheidung.HART;
            case LOGISCH -> dieterbaier.standardschreiben.spi.Loeschentscheidung.LOGISCH;
            case GESPERRT -> dieterbaier.standardschreiben.spi.Loeschentscheidung.GESPERRT;
        };
    }

    static Loeschentscheidung zurDomaene(dieterbaier.standardschreiben.spi.Loeschentscheidung entscheidung) {
        return switch (entscheidung) {
            case HART -> Loeschentscheidung.HART;
            case LOGISCH -> Loeschentscheidung.LOGISCH;
            case GESPERRT -> Loeschentscheidung.GESPERRT;
        };
    }

    static dieterbaier.standardschreiben.spi.Datenkontext zumSpi(Datenkontext datenkontext) {
        return new dieterbaier.standardschreiben.spi.Datenkontext(datenkontext.werte());
    }

    /**
     * Übernimmt die Werte des Hosts ins Domänenmodell, das sie prüft.
     *
     * @throws HostVertragVerletzt wenn die Werte die Regeln des Datenkontexts verletzen
     */
    static Datenkontext zurDomaene(dieterbaier.standardschreiben.spi.Datenkontext datenkontext) {
        try {
            return Datenkontext.aus(datenkontext.werte());
        } catch (Validierungsfehler fehler) {
            throw new HostVertragVerletzt("Der PlatzhalterdatenAnbieter des Hosts hat einen ungültigen Datenkontext"
                    + " geliefert: " + fehler.getMessage(), fehler);
        }
    }

    static Standardschreibenkennung kennung(Standardschreiben standardschreiben) {
        return new Standardschreibenkennung(standardschreiben.id().wert(), standardschreiben.name());
    }

    static Datenanfrage datenanfrage(BenutzerKennung benutzer, FachlicherBezug bezug, Standardschreiben vorlage) {
        return new Datenanfrage(benutzer.wert(), zumSpi(bezug), kennung(vorlage));
    }

    static Loeschanfrage loeschanfrage(BenutzerKennung benutzer, Schreiben schreiben) {
        return new Loeschanfrage(benutzer.wert(), schreiben.id().wert(), zumSpi(schreiben.bezug()),
                schreiben.erstellt());
    }
}
