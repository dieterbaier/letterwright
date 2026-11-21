package dieterbaier.standardschreiben.domaene.modell;

import java.io.Serial;

/**
 * Eine Vorlage soll etwas tun, das der Zustand ihrer Versionen nicht erlaubt, etwa eine veröffentlichte Version ändern.
 * Anders als ein {@link dieterbaier.standardschreiben.validierung.Validierungsfehler} ist das kein ungültiger Wert,
 * sondern ein fachlich verbotener Schritt.
 */
public final class UnzulaessigerZustandswechsel extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    UnzulaessigerZustandswechsel(VorlagenId vorlage, String meldung) {
        super("Vorlage " + vorlage.wert() + ": " + meldung);
    }
}
