package dieterbaier.standardschreiben.spi;

/** Wie ein Schreiben gelöscht wird. Lösch- und Aufbewahrungsregeln bestimmt der Host. */
public enum Loeschentscheidung {

    /** Das Schreiben und sein PDF werden entfernt. */
    HART,

    /** Das Schreiben wird als gelöscht markiert und nicht mehr angezeigt; das PDF bleibt erhalten. */
    LOGISCH,

    /** Das Schreiben darf nicht gelöscht werden, etwa wegen einer Aufbewahrungsfrist. */
    GESPERRT
}
