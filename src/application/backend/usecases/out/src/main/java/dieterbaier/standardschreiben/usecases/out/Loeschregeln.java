package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.Loeschentscheidung;
import dieterbaier.standardschreiben.domaene.modell.Schreiben;

/** Fragt die Löschrichtlinie des Hosts. Implementiert von {@code adapter-host} (ADR-016). */
public interface Loeschregeln {

    /** Wie das Schreiben gelöscht wird, wenn der Benutzer es löschen will. */
    Loeschentscheidung entscheide(BenutzerKennung benutzer, Schreiben schreiben);
}
