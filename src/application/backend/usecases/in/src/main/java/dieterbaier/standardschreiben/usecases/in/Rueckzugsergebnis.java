package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.validierung.Pruefe;

/** Was beim Zurückziehen einer Vorlagenversion herauskommt. Außer bei {@link Zurueckgezogen} bleibt alles, wie es war. */
public sealed interface Rueckzugsergebnis {

    record Zurueckgezogen(Versionsverweis version) implements Rueckzugsergebnis {

        public Zurueckgezogen {
            Pruefe.pflicht(version, "Die zurückgezogene Version");
        }
    }

    record NichtErlaubt() implements Rueckzugsergebnis {}

    record VorlageUnbekannt() implements Rueckzugsergebnis {}

    record NichtsVeroeffentlicht() implements Rueckzugsergebnis {}

    /** Die Vorlage hat sich seit dem Laden geändert, etwa durch einen anderen Vorlagenverwalter. */
    record Veraltet() implements Rueckzugsergebnis {}
}
