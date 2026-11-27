package dieterbaier.letterwright.domain.model.snapshot;

import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Ein Snapshot ist offen, bis aus ihm ein Schreiben entsteht; danach ist er verbraucht (ADR-021). Nur ein offener
 * Snapshot hält den Datenkontext: Ist das Schreiben gespeichert, braucht ihn niemand mehr.
 */
@ValueObject
public sealed interface SnapshotState {

    record Open(DataContext dataContext) implements SnapshotState {

        public Open {
            Validate.required(dataContext, "Der Datenkontext des Snapshots");
        }

        /** Ob der neu gelesene Datenkontext dieselbe Prüfsumme hat wie der der Vorschau. */
        public boolean isUnchanged(DataContext reread) {
            return dataContext.checksum().equals(reread.checksum());
        }
    }

    /**
     * Aus dem Snapshot ist ein Schreiben entstanden. Er bleibt bis zu seinem Ablauf, damit eine Wiederholung Besitzer und
     * Berechtigung prüfen und das Schreiben finden kann.
     */
    record Consumed() implements SnapshotState {}
}
