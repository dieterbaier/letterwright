package dieterbaier.standardschreiben.domaene.modell.schreiben;

import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.validierung.Pruefe;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Ein neues Schreiben und der Snapshot, den es verbraucht hat. Beide werden zusammen gespeichert: Nur wenn der
 * gespeicherte Snapshot dabei noch offen ist, gilt das Schreiben (ADR-021).
 */
@ValueObject
public record SchreibenAusSnapshot(Schreiben schreiben, Snapshot verbrauchterSnapshot) {

    public SchreibenAusSnapshot {
        Pruefe.pflicht(schreiben, "Das Schreiben");
        Pruefe.pflicht(verbrauchterSnapshot, "Der verbrauchte Snapshot");
    }
}
