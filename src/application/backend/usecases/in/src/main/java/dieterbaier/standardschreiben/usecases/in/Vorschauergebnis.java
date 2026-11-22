package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.validierung.Pruefe;
import java.time.Instant;

/** Was beim Erzeugen einer Vorschau herauskommt. Nur {@link Erzeugt} enthält eine Vorschau. */
public sealed interface Vorschauergebnis {

    /**
     * Die Vorschau als HTML, noch nicht paginiert. Mit der Snapshot-ID wird das Schreiben später gespeichert, bis zum
     * Ablauf.
     */
    record Erzeugt(SnapshotId snapshot, String html, Instant ablauf) implements Vorschauergebnis {

        public Erzeugt {
            Pruefe.pflicht(snapshot, "Der Snapshot der Vorschau");
            Pruefe.pflicht(html, "Das HTML der Vorschau");
            Pruefe.pflicht(ablauf, "Der Ablauf der Vorschau");
        }
    }

    /** Ein Standardschreiben mit dieser ID gibt es nicht. */
    record StandardschreibenUnbekannt() implements Vorschauergebnis {}

    /** Das Standardschreiben gehört zu einem anderen Bezugstyp (ADR-020). Der Host wurde nicht gefragt. */
    record BezugPasstNicht() implements Vorschauergebnis {}

    /** Standardschreiben oder Briefpapier haben keine veröffentlichte Version. */
    record KeineVeroeffentlichteVersion() implements Vorschauergebnis {}

    /** Der Host erlaubt dem Benutzer nicht, aus dem Standardschreiben zu dem Bezug ein Schreiben zu erzeugen. */
    record NichtErlaubt() implements Vorschauergebnis {}
}
