package dieterbaier.standardschreiben.spi;

/** Eine Handlung, für die der Host die Berechtigung erteilt. */
public enum Aktion {

    /** Briefpapier und Standardschreiben pflegen. Die Anfrage hat keinen fachlichen Bezug. */
    VORLAGEN_PFLEGEN,

    /** Zu einem fachlichen Bezug eine Vorschau erzeugen und ein Schreiben speichern. */
    SCHREIBEN_ERZEUGEN,

    /** Die Schreiben zu einem fachlichen Bezug in der Übersicht sehen. */
    SCHREIBEN_ANSEHEN,

    /** Ein Schreiben zu einem fachlichen Bezug herunterladen. */
    SCHREIBEN_HERUNTERLADEN,

    /** Ein Schreiben zu einem fachlichen Bezug löschen. Wie, entscheidet die {@link Loeschrichtlinie}. */
    SCHREIBEN_LOESCHEN;

    /** Ob die Anfrage zu dieser Aktion einen fachlichen Bezug hat. */
    public boolean hatBezug() {
        return this != VORLAGEN_PFLEGEN;
    }
}
