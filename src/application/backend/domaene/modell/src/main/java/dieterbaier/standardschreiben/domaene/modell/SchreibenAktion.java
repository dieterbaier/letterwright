package dieterbaier.standardschreiben.domaene.modell;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Eine Handlung an den gespeicherten Schreiben zu einem {@link FachlicherBezug}. Das Erzeugen gehört nicht dazu, weil
 * der Host dafür auch das Standardschreiben kennen muss (ADR-020).
 */
@ValueObject
public enum SchreibenAktion {

    ANSEHEN,

    HERUNTERLADEN,

    /** Wie gelöscht wird, entscheidet die Löschrichtlinie des Hosts. */
    LOESCHEN
}
