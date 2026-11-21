package dieterbaier.standardschreiben.adapter.out.host;

import dieterbaier.standardschreiben.domaene.modell.Aktion;
import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Berechtigungspruefung;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.validierung.Pruefe;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;

/** Fragt die {@link Berechtigungspruefung} des Hosts, oder die Default-Implementierung des Starters. */
public final class HostBerechtigungen implements Berechtigungen {

    private final Berechtigungspruefung pruefung;

    public HostBerechtigungen(Berechtigungspruefung pruefung) {
        this.pruefung = Pruefe.pflicht(pruefung, "Die Berechtigungspruefung");
    }

    @Override
    public boolean darf(BenutzerKennung benutzer, Aktion aktion, FachlicherBezug bezug) {
        if (!Pruefe.pflicht(aktion, "Die Aktion").brauchtBezug()) {
            throw new Validierungsfehler("Die Aktion " + aktion + " bezieht sich auf keinen fachlichen Bezug");
        }
        return pruefung.istErlaubt(new Berechtigungsanfrage(Pruefe.pflicht(benutzer, "Der Benutzer").wert(),
                Abbildung.zumSpi(aktion), Abbildung.zumSpi(Pruefe.pflicht(bezug, "Der fachliche Bezug"))));
    }

    @Override
    public boolean darfVorlagenPflegen(BenutzerKennung benutzer) {
        return pruefung.istErlaubt(new Berechtigungsanfrage(Pruefe.pflicht(benutzer, "Der Benutzer").wert(),
                dieterbaier.standardschreiben.spi.Aktion.VORLAGEN_PFLEGEN, null));
    }
}
