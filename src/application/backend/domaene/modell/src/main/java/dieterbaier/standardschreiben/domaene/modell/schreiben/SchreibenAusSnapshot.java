package dieterbaier.standardschreiben.domaene.modell.schreiben;

import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshot;
import dieterbaier.standardschreiben.domaene.modell.snapshot.Snapshotzustand;
import dieterbaier.standardschreiben.validierung.Pruefe;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;
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
        if (!(verbrauchterSnapshot.zustand() instanceof Snapshotzustand.Verbraucht)) {
            throw new Validierungsfehler("Der Snapshot " + verbrauchterSnapshot.id().wert() + " ist nicht verbraucht");
        }
        if (!schreiben.snapshot().equals(verbrauchterSnapshot.id())) {
            throw new Validierungsfehler("Das Schreiben " + schreiben.id().wert() + " entstand nicht aus Snapshot "
                    + verbrauchterSnapshot.id().wert());
        }
    }
}
