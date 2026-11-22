package dieterbaier.standardschreiben.domaene.modell.snapshot;

import dieterbaier.standardschreiben.domaene.modell.Datenkontext;
import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Ein Snapshot ist offen, bis aus ihm ein Schreiben entsteht; danach ist er verbraucht (ADR-021). Nur ein offener
 * Snapshot hält den Datenkontext: Ist das Schreiben gespeichert, braucht ihn niemand mehr.
 */
@ValueObject
public sealed interface Snapshotzustand {

    record Offen(Datenkontext datenkontext) implements Snapshotzustand {

        public Offen {
            Pruefe.pflicht(datenkontext, "Der Datenkontext des Snapshots");
        }

        /** Ob der neu gelesene Datenkontext dieselbe Prüfsumme hat wie der der Vorschau. */
        public boolean istUnveraendert(Datenkontext neuGelesen) {
            return datenkontext.pruefsumme().equals(neuGelesen.pruefsumme());
        }
    }

    /**
     * Aus dem Snapshot ist ein Schreiben entstanden. Er bleibt bis zu seinem Ablauf, damit eine Wiederholung Besitzer und
     * Berechtigung prüfen und das Schreiben finden kann.
     */
    record Verbraucht() implements Snapshotzustand {}
}
