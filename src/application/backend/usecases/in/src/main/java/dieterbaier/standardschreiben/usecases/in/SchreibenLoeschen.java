package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;

/**
 * Anwendungsfall „Schreiben löschen“: Ob und wie gelöscht wird, entscheidet die Löschrichtlinie des Hosts (RB-10,
 * ADR-007). Ein Schreiben, das der Benutzer nicht löschen darf, gibt es für ihn nicht (QS-009).
 */
public interface SchreibenLoeschen {

    Loeschergebnis loesche(BenutzerKennung benutzer, SchreibenId schreiben);
}
