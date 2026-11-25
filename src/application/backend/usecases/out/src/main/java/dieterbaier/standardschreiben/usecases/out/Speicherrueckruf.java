package dieterbaier.standardschreiben.usecases.out;

import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Rueckrufergebnis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;

/**
 * Meldet dem Host ein neu angelegtes Schreiben, vor dem Commit der Transaktion (ADR-023). Implementiert von
 * {@code adapter-host}; stellt der Host keinen Rückruf bereit, nimmt die Implementierung jedes Schreiben an.
 */
public interface Speicherrueckruf {

    Rueckrufergebnis angelegt(BenutzerKennung benutzer, Schreiben schreiben);
}
