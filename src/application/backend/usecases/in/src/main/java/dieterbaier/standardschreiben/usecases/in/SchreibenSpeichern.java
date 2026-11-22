package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;

/**
 * Anwendungsfall „Schreiben speichern“: Aus dem Snapshot einer Vorschau entsteht das PDF, das der Benutzer gesehen hat,
 * und wird nur gespeichert, wenn es konform ist (ADR-005, ADR-007). Derselbe Snapshot ergibt höchstens ein Schreiben;
 * ein neues entsteht nur aus einer neuen Vorschau (ADR-021).
 */
public interface SchreibenSpeichern {

    Speicherergebnis speichere(BenutzerKennung benutzer, SnapshotId snapshot);
}
