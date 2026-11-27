package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.validation.Validate;
import java.time.Instant;

/** Was beim Erzeugen einer Vorschau herauskommt. Nur {@link Created} enthält eine Vorschau. */
public sealed interface PreviewResult {

    /**
     * Die Vorschau als HTML, noch nicht paginiert. Mit der Snapshot-ID wird das Schreiben später gespeichert, bis zum
     * Ablauf.
     */
    record Created(SnapshotId snapshot, String html, Instant expiry) implements PreviewResult {

        public Created {
            Validate.required(snapshot, "Der Snapshot der Vorschau");
            Validate.required(html, "Das HTML der Vorschau");
            Validate.required(expiry, "Der Ablauf der Vorschau");
        }
    }

    /** Ein Standardschreiben mit dieser ID gibt es nicht. */
    record LetterTemplateUnknown() implements PreviewResult {}

    /** Das Standardschreiben gehört zu einem anderen Bezugstyp (ADR-020). Der Host wurde nicht gefragt. */
    record ReferenceMismatch() implements PreviewResult {}

    /** Standardschreiben oder Briefpapier haben keine veröffentlichte Version. */
    record NoPublishedVersion() implements PreviewResult {}

    /** Der Host erlaubt dem Benutzer nicht, aus dem Standardschreiben zu dem Bezug ein Schreiben zu erzeugen. */
    record NotAllowed() implements PreviewResult {}
}
