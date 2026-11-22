package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Loeschentscheidung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;

/** Fragt die Löschrichtlinie des Hosts. Implementiert von {@code adapter-host} (ADR-016). */
public interface Loeschregeln {

    Loeschentscheidung entscheide(BenutzerKennung benutzer, Schreiben schreiben);
}
