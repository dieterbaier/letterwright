package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.validierung.Pruefe;

/** Sicherer Ausgang von {@link SchreibenRepository#speichere}. Ein ungewisser Ausgang ist eine Ausnahme. */
public sealed interface Speicherausgang {

    record Gespeichert() implements Speicherausgang {}

    /** Gespeichert wurde nichts; aus dem Snapshot ist schon dieses Schreiben entstanden. */
    record SnapshotBereitsVerbraucht(Schreiben vorhandenes) implements Speicherausgang {

        public SnapshotBereitsVerbraucht {
            Pruefe.pflicht(vorhandenes, "Das vorhandene Schreiben");
        }
    }
}
