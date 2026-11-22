package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.validierung.Pruefe;

/** Sicherer Ausgang von {@link SchreibenRepository#speichere}. Ein ungewisser Ausgang ist eine Ausnahme. */
public sealed interface Speicherausgang {

    record Gespeichert() implements Speicherausgang {}

    /** Gespeichert wurde nichts; den Snapshot gibt es nicht mehr, etwa weil eine neue Vorschau ihn ersetzt hat. */
    record SnapshotNichtMehrVorhanden() implements Speicherausgang {}

    /**
     * Gespeichert wurde nichts; aus dem Snapshot ist schon dieses Schreiben entstanden. Es kann inzwischen logisch
     * gelöscht sein.
     */
    record SnapshotBereitsVerbraucht(Schreiben vorhandenes) implements Speicherausgang {

        public SnapshotBereitsVerbraucht {
            Pruefe.pflicht(vorhandenes, "Das vorhandene Schreiben");
        }
    }

    /** Gespeichert wurde nichts; aus dem Snapshot ist schon ein Schreiben entstanden, und es ist hart gelöscht. */
    record SchreibenEntfernt() implements Speicherausgang {}
}
