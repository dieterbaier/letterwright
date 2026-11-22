package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Seitenanfrage;

/**
 * Anwendungsfall „Schreiben auflisten“: die sichtbaren Schreiben zu einem fachlichen Bezug, seitenweise. Was der
 * Benutzer nicht sehen darf, zählt weder in die Seite noch in die Gesamtzahl (QS-009).
 */
public interface SchreibenAuflisten {

    Uebersichtsergebnis liste(BenutzerKennung benutzer, FachlicherBezug bezug, Seitenanfrage seite);
}
