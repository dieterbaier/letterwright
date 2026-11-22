package dieterbaier.standardschreiben.usecases.service;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.SchreibenAktion;
import dieterbaier.standardschreiben.domaene.modell.Seitenanfrage;
import dieterbaier.standardschreiben.usecases.in.SchreibenAuflisten;
import dieterbaier.standardschreiben.usecases.in.Uebersichtsergebnis;
import dieterbaier.standardschreiben.usecases.out.Berechtigungen;
import dieterbaier.standardschreiben.usecases.out.SchreibenRepository;

/**
 * Fragt den Host vor dem Repository: Ohne Berechtigung wird nichts gelesen und nichts gezählt. Der Host erteilt sie je
 * Bezug; logisch gelöschte Schreiben filtert das Repository vor dem Paginieren (QS-009).
 */
public final class Schreibenuebersicht implements SchreibenAuflisten {

    private final Berechtigungen berechtigungen;
    private final SchreibenRepository schreiben;

    public Schreibenuebersicht(Berechtigungen berechtigungen, SchreibenRepository schreiben) {
        this.berechtigungen = berechtigungen;
        this.schreiben = schreiben;
    }

    @Override
    public Uebersichtsergebnis liste(BenutzerKennung benutzer, FachlicherBezug bezug, Seitenanfrage seite) {
        if (!berechtigungen.darf(benutzer, SchreibenAktion.ANSEHEN, bezug)) {
            return new Uebersichtsergebnis.NichtErlaubt();
        }
        return new Uebersichtsergebnis.Gefunden(schreiben.sichtbareZu(bezug, seite));
    }
}
