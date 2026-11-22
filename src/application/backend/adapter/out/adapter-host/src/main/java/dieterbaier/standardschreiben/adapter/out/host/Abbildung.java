package dieterbaier.standardschreiben.adapter.out.host;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Loeschentscheidung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
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

    static dieterbaier.standardschreiben.spi.VorlagenAktion zumSpi(VorlagenAktion aktion) {
        return switch (aktion) {
            case BEARBEITEN -> dieterbaier.standardschreiben.spi.VorlagenAktion.BEARBEITEN;
            case VEROEFFENTLICHEN -> dieterbaier.standardschreiben.spi.VorlagenAktion.VEROEFFENTLICHEN;
        };
    }

    static dieterbaier.standardschreiben.spi.SchreibenAktion zumSpi(SchreibenAktion aktion) {
        return switch (aktion) {
            case ERZEUGEN -> dieterbaier.standardschreiben.spi.SchreibenAktion.ERZEUGEN;
            case ANSEHEN -> dieterbaier.standardschreiben.spi.SchreibenAktion.ANSEHEN;
            case HERUNTERLADEN -> dieterbaier.standardschreiben.spi.SchreibenAktion.HERUNTERLADEN;
            case LOESCHEN -> dieterbaier.standardschreiben.spi.SchreibenAktion.LOESCHEN;
        };
    }

    static dieterbaier.standardschreiben.spi.Loeschentscheidung zumSpi(Loeschentscheidung entscheidung) {
        return switch (entscheidung) {
            case Loeschentscheidung.Hart hart -> dieterbaier.standardschreiben.spi.Loeschentscheidung.hart();
            case Loeschentscheidung.Logisch logisch -> dieterbaier.standardschreiben.spi.Loeschentscheidung.logisch();
            case Loeschentscheidung.Gesperrt gesperrt ->
                    dieterbaier.standardschreiben.spi.Loeschentscheidung.gesperrt(gesperrt.grund());
        };
    }

    static Loeschentscheidung zurDomaene(dieterbaier.standardschreiben.spi.Loeschentscheidung entscheidung) {
        return switch (entscheidung) {
            case dieterbaier.standardschreiben.spi.Loeschentscheidung.Hart hart -> Loeschentscheidung.hart();
            case dieterbaier.standardschreiben.spi.Loeschentscheidung.Logisch logisch -> Loeschentscheidung.logisch();
            case dieterbaier.standardschreiben.spi.Loeschentscheidung.Gesperrt gesperrt ->
                    Loeschentscheidung.gesperrt(gesperrt.grund());
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
