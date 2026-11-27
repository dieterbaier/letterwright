package dieterbaier.letterwright.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Eine Handlung an den gespeicherten Schreiben zu einem {@link BusinessReference}. Das Erzeugen gehört nicht dazu, weil
 * der Host dafür auch das Standardschreiben kennen muss (ADR-020).
 */
@ValueObject
public enum LetterAction {

    VIEW,

    DOWNLOAD,

    /** Wie gelöscht wird, entscheidet die Löschrichtlinie des Hosts. */
    DELETE
}
