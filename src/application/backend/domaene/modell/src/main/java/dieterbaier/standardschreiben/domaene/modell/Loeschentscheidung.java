package dieterbaier.standardschreiben.domaene.modell;

import org.jmolecules.ddd.annotation.ValueObject;

/** Wie ein Schreiben gelöscht wird. Lösch- und Aufbewahrungsregeln bestimmt die Host-Anwendung (RB-10, ADR-007). */
@ValueObject
public enum Loeschentscheidung {

    /** Das Schreiben und sein PDF werden entfernt. */
    HART,

    /** Das Schreiben wird als gelöscht markiert und nicht mehr angezeigt; das PDF bleibt erhalten. */
    LOGISCH,

    /** Das Schreiben darf nicht gelöscht werden, etwa wegen einer Aufbewahrungsfrist. */
    GESPERRT
}
