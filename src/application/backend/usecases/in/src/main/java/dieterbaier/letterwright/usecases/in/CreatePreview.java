package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.snapshot.SnapshotId;
import dieterbaier.letterwright.domain.model.template.TemplateId;

/**
 * Anwendungsfall „Vorschau erzeugen“: Ein Sachbearbeiter sieht ein Standardschreiben zu einem fachlichen Bezug, bevor
 * er es speichert. Jede Vorschau legt einen Snapshot an (ADR-007).
 */
public interface CreatePreview {

    PreviewResult create(UserId user, BusinessReference reference, TemplateId letterTemplate);

    /**
     * Wie {@link #create}, ersetzt aber den Snapshot der bisherigen Vorschau. Gehört er einem anderen Benutzer, gibt
     * es ihn nicht oder ist aus ihm schon ein Schreiben entstanden, bleibt er unberührt.
     */
    PreviewResult update(
            UserId user, BusinessReference reference, TemplateId letterTemplate, SnapshotId previous);
}
