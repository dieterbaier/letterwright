package dieterbaier.standardschreiben.validierung;

import java.io.Serial;

/** Ein Wert erfüllt eine Prüfung nicht. Die Meldung nennt das Attribut und die verletzte Regel. */
public final class Validierungsfehler extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 1L;

    public Validierungsfehler(String meldung) {
        super(meldung);
    }

    public Validierungsfehler(String meldung, Throwable ursache) {
        super(meldung, ursache);
    }
}
