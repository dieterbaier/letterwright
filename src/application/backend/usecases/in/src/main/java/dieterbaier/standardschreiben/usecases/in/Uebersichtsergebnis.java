package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.Seite;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.validierung.Pruefe;

/** Was beim Auflisten der Schreiben zu einem Bezug herauskommt. */
public sealed interface Uebersichtsergebnis {

    record Gefunden(Seite<Schreiben> seite) implements Uebersichtsergebnis {

        public Gefunden {
            Pruefe.pflicht(seite, "Die Seite der Übersicht");
        }
    }

    /** Der Host erlaubt dem Benutzer nicht, die Schreiben zu dem Bezug anzusehen. Gezählt wurde nichts. */
    record NichtErlaubt() implements Uebersichtsergebnis {}
}
