package dieterbaier.standardschreiben.domaene.modell;

import org.jmolecules.ddd.annotation.ValueObject;

/** Eine Handlung an den Schreiben zu einem {@link FachlicherBezug}. */
@ValueObject
public enum SchreibenAktion implements Aktion {

    /** Umfasst Vorschau und Speichern. */
    ERZEUGEN,

    ANSEHEN,

    HERUNTERLADEN,

    /** Wie gelöscht wird, entscheidet die Löschrichtlinie des Hosts. */
    LOESCHEN
}
