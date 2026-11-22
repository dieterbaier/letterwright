package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;

/**
 * Anwendungsfall „Vorschau erzeugen“: Ein Sachbearbeiter sieht ein Standardschreiben zu einem fachlichen Bezug, bevor
 * er es speichert. Jede Vorschau legt einen Snapshot an (ADR-007).
 */
public interface VorschauErzeugen {

    Vorschauergebnis erzeuge(BenutzerKennung benutzer, FachlicherBezug bezug, VorlagenId standardschreiben);

    /**
     * Wie {@link #erzeuge}, ersetzt aber den Snapshot der bisherigen Vorschau. Gehört er einem anderen Benutzer, gibt
     * es ihn nicht oder ist aus ihm schon ein Schreiben entstanden, bleibt er unberührt.
     */
    Vorschauergebnis aktualisiere(
            BenutzerKennung benutzer, FachlicherBezug bezug, VorlagenId standardschreiben, SnapshotId bisheriger);
}
