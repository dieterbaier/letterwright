package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;

/** Legt das PDF eines Schreibens ab. Was ein Dokumentverweis bedeutet, weiß nur die Ablage. */
public interface DokumentAblage {

    Dokumentverweis lege(byte[] pdf);

    /** Für ein abgelegtes PDF, zu dem kein Schreiben gespeichert wurde. */
    void entferne(Dokumentverweis dokument);
}
