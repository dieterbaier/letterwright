package dieterbaier.standardschreiben.adapter.out.host;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.VorlagenAktion;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Standardschreiben;
import dieterbaier.standardschreiben.spi.Berechtigungsanfrage;
import dieterbaier.standardschreiben.spi.Berechtigungspruefung;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;

/** Fragt die {@link Berechtigungspruefung} des Hosts, oder die Default-Implementierung des Starters. */
public final class HostBerechtigungen implements Berechtigungen {

    private final Berechtigungspruefung pruefung;

    public HostBerechtigungen(Berechtigungspruefung pruefung) {
        this.pruefung = pruefung;
    }

    @Override
    public boolean darf(BenutzerKennung benutzer, VorlagenAktion aktion) {
        return pruefung.istErlaubt(Berechtigungsanfrage.zuVorlagen(benutzer.wert(), Abbildung.zumSpi(aktion)));
    }

    @Override
    public boolean darf(BenutzerKennung benutzer, SchreibenAktion aktion, FachlicherBezug bezug) {
        return pruefung.istErlaubt(Berechtigungsanfrage.zuSchreiben(benutzer.wert(), Abbildung.zumSpi(aktion),
                Abbildung.zumSpi(bezug)));
    }

    @Override
    public boolean darfErzeugen(BenutzerKennung benutzer, FachlicherBezug bezug, Standardschreiben standardschreiben) {
        return pruefung.istErlaubt(Berechtigungsanfrage.zumErzeugen(benutzer.wert(), Abbildung.zumSpi(bezug),
                Abbildung.kennung(standardschreiben)));
    }
}
