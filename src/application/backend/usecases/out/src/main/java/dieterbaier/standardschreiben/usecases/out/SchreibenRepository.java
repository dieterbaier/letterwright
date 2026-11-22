package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import java.util.Optional;

/** Speichert und lädt Schreiben. Zu einem Snapshot gibt es höchstens ein Schreiben (ADR-021). */
public interface SchreibenRepository {

    Optional<Schreiben> zuSnapshot(SnapshotId snapshot);

    /**
     * Speichert das Schreiben, wenn es zu seinem Snapshot noch keines gibt, und gibt es zurück. Gibt es schon eines,
     * auch aus einer gleichzeitigen Anfrage, speichert es nichts und gibt das vorhandene zurück. Beides geschieht
     * atomar.
     */
    Schreiben speichereEinmalig(Schreiben schreiben);
}
