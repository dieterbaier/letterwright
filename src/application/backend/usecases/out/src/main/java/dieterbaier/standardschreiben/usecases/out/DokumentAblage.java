package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;

/** Legt das PDF eines Schreibens ab. Was ein Dokumentverweis bedeutet, weiß nur die Ablage. */
public interface DokumentAblage {

    Dokumentverweis lege(byte[] pdf);

    /**
     * Die Bytes, wie sie abgelegt wurden.
     *
     * @throws IllegalStateException wenn es zum Verweis kein PDF gibt
     */
    byte[] lies(Dokumentverweis dokument);

    /** Für ein PDF, auf das kein Schreiben (mehr) verweist: nie gespeichert oder hart gelöscht. */
    void entferne(Dokumentverweis dokument);
}
