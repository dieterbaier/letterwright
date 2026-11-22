package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.validierung.Pruefe;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;
import java.util.List;

/** Ergebnis einer Konformitätsprüfung. */
public sealed interface Konformitaet {

    static Konformitaet konform() {
        return new Konform();
    }

    static Konformitaet nichtKonform(List<String> befunde) {
        return new NichtKonform(befunde);
    }

    record Konform() implements Konformitaet {}

    /** Mindestens ein Befund, damit der Benutzer erfährt, was nicht stimmt. */
    record NichtKonform(List<String> befunde) implements Konformitaet {

        public NichtKonform {
            befunde = List.copyOf(Pruefe.pflichtOhneLuecken(befunde, "Die Befunde der Konformitätsprüfung"));
            if (befunde.isEmpty()) {
                throw new Validierungsfehler("Ein nicht konformes PDF braucht mindestens einen Befund");
            }
        }
    }
}
