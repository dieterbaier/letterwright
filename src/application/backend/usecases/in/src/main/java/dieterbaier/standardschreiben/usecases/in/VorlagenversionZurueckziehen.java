package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;

/**
 * Anwendungsfall „Vorlagenversion zurückziehen“: Die veröffentlichte Version eines Briefpapiers oder Standardschreibens
 * wird ohne Nachfolger zurückgenommen. Bis zur nächsten Veröffentlichung entsteht mit der Vorlage kein neues Schreiben;
 * gespeicherte Schreiben bleiben unverändert (ADR-007, ADR-022).
 */
public interface VorlagenversionZurueckziehen {

    Rueckzugsergebnis ziehZurueck(BenutzerKennung benutzer, VorlagenId vorlage);
}
