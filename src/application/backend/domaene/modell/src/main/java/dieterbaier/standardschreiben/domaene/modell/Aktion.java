package dieterbaier.standardschreiben.domaene.modell;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Eine Handlung, für die die Host-Anwendung die Berechtigung erteilt (ADR-002). Rollen und Sichtbarkeit bestimmt der
 * Host (Q-PO-003); die Bibliothek fragt je Handlung.
 */
@ValueObject
public enum Aktion {

    /** Briefpapier und Standardschreiben anlegen, Entwürfe ändern und neue Entwürfe anlegen. Ohne fachlichen Bezug. */
    VORLAGEN_BEARBEITEN(false),

    /**
     * Entwürfe veröffentlichen und veröffentlichte Versionen zurückziehen. Ohne fachlichen Bezug. Getrennt vom Bearbeiten,
     * damit der Host ein Vier-Augen-Prinzip abbilden kann: Was veröffentlicht ist, geht in alle neuen Schreiben ein.
     */
    VORLAGEN_VEROEFFENTLICHEN(false),

    /** Zu einem fachlichen Bezug eine Vorschau erzeugen und ein Schreiben speichern. */
    SCHREIBEN_ERZEUGEN(true),

    /** Die Schreiben zu einem fachlichen Bezug in der Übersicht sehen. */
    SCHREIBEN_ANSEHEN(true),

    /** Ein Schreiben zu einem fachlichen Bezug herunterladen. */
    SCHREIBEN_HERUNTERLADEN(true),

    /** Ein Schreiben zu einem fachlichen Bezug löschen; wie, entscheidet die Löschrichtlinie. */
    SCHREIBEN_LOESCHEN(true);

    private final boolean brauchtBezug;

    Aktion(boolean brauchtBezug) {
        this.brauchtBezug = brauchtBezug;
    }

    /** Ob die Handlung sich auf einen fachlichen Bezug bezieht. */
    public boolean brauchtBezug() {
        return brauchtBezug;
    }
}
