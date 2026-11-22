package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;

/**
 * Anwendungsfall „Schreiben herunterladen“: das unveränderte PDF eines gespeicherten Schreibens (ADR-007, QS-008). Ein
 * Schreiben, das der Benutzer nicht herunterladen darf, gibt es für ihn nicht (QS-009).
 */
public interface SchreibenHerunterladen {

    Downloadergebnis lade(BenutzerKennung benutzer, SchreibenId schreiben);
}
