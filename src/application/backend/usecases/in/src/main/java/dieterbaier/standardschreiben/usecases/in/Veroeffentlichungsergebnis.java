package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.validierung.Pruefe;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;
import java.util.List;

/** Was beim Veröffentlichen einer Vorlagenversion herauskommt. Außer bei {@link Veroeffentlicht} bleibt alles, wie es war. */
public sealed interface Veroeffentlichungsergebnis {

    /** Diese Version ist jetzt die veröffentlichte; neue Schreiben entstehen mit ihr. */
    record Veroeffentlicht(Versionsverweis version) implements Veroeffentlichungsergebnis {

        public Veroeffentlicht {
            Pruefe.pflicht(version, "Die veröffentlichte Version");
        }
    }

    /** Das Probe-PDF ist nicht konform; der Entwurf bleibt ein Entwurf. Die Befunde sind für den Vorlagenverwalter. */
    record NichtKonform(List<String> befunde) implements Veroeffentlichungsergebnis {

        public NichtKonform {
            befunde = List.copyOf(Pruefe.pflichtOhneLuecken(befunde, "Die Befunde der Konformitätsprüfung"));
            if (befunde.isEmpty()) {
                throw new Validierungsfehler("Ein nicht konformes Probe-PDF braucht mindestens einen Befund");
            }
        }
    }

    record NichtErlaubt() implements Veroeffentlichungsergebnis {}

    record VorlageUnbekannt() implements Veroeffentlichungsergebnis {}

    record KeinEntwurf() implements Veroeffentlichungsergebnis {}

    /** Der Entwurf ist leer oder enthält nur Leerraum. */
    record EntwurfOhneInhalt() implements Veroeffentlichungsergebnis {}

    /** Ein Standardschreiben braucht für sein Probe-PDF die veröffentlichte Version seines Briefpapiers. */
    record BriefpapierNichtVeroeffentlicht() implements Veroeffentlichungsergebnis {}

    /** Die Vorlage hat sich seit dem Laden geändert, etwa durch einen anderen Vorlagenverwalter. Neu laden und prüfen. */
    record Veraltet() implements Veroeffentlichungsergebnis {}
}
