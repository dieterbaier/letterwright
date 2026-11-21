package dieterbaier.standardschreiben.spi;

/** Eine Handlung, für die der Host die Berechtigung erteilt. */
public enum Aktion {

    /** Briefpapier und Standardschreiben anlegen, Entwürfe ändern und neue Entwürfe anlegen. Ohne fachlichen Bezug. */
    VORLAGEN_BEARBEITEN(false),

    /**
     * Entwürfe veröffentlichen und veröffentlichte Versionen zurückziehen. Ohne fachlichen Bezug. Getrennt vom Bearbeiten,
     * damit der Host ein Vier-Augen-Prinzip abbilden kann.
     */
    VORLAGEN_VEROEFFENTLICHEN(false),

    /** Zu einem fachlichen Bezug eine Vorschau erzeugen und ein Schreiben speichern. */
    SCHREIBEN_ERZEUGEN(true),

    /** Die Schreiben zu einem fachlichen Bezug in der Übersicht sehen. */
    SCHREIBEN_ANSEHEN(true),

    /** Ein Schreiben zu einem fachlichen Bezug herunterladen. */
    SCHREIBEN_HERUNTERLADEN(true),

    /** Ein Schreiben zu einem fachlichen Bezug löschen. Wie, entscheidet die {@link Loeschrichtlinie}. */
    SCHREIBEN_LOESCHEN(true);

    private final boolean hatBezug;

    Aktion(boolean hatBezug) {
        this.hatBezug = hatBezug;
    }

    /** Ob die Anfrage zu dieser Aktion einen fachlichen Bezug hat. */
    public boolean hatBezug() {
        return hatBezug;
    }
}
