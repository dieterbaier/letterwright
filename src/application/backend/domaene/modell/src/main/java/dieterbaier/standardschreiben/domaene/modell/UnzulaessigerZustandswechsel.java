package dieterbaier.standardschreiben.domaene.modell;

import java.io.Serial;

/** Eine Vorlagenversion soll etwas tun, das ihr Zustand nicht erlaubt. */
public final class UnzulaessigerZustandswechsel extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    UnzulaessigerZustandswechsel(Versionsverweis version, Vorlagenzustand zustand, String vorgang) {
        super("Version " + version.nummer().wert() + " der Vorlage " + version.vorlage().wert()
                + " ist " + zustand + ": " + vorgang + " ist nicht erlaubt");
    }
}
