package dieterbaier.standardschreiben.usecases.in;

import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.validierung.Pruefe;

/** Was beim Herunterladen eines Schreibens herauskommt. Nur {@link Geliefert} enthält ein PDF. */
public sealed interface Downloadergebnis {

    /**
     * Das PDF mit derselben Prüfsumme wie beim Speichern. Eine Klasse statt eines Records, weil ein Record ein Array per
     * Referenz vergleicht und herausgibt.
     */
    final class Geliefert implements Downloadergebnis {

        private final Schreiben schreiben;
        private final byte[] pdf;

        public Geliefert(Schreiben schreiben, byte[] pdf) {
            this.schreiben = Pruefe.pflicht(schreiben, "Das Schreiben");
            this.pdf = Pruefe.pflicht(pdf, "Das PDF").clone();
        }

        public Schreiben schreiben() {
            return schreiben;
        }

        public byte[] pdf() {
            return pdf.clone();
        }
    }

    /**
     * Das Schreiben gibt es nicht, es ist logisch gelöscht, oder der Benutzer darf es nicht herunterladen. Die Fälle sind
     * absichtlich nicht unterscheidbar (QS-009).
     */
    record Unbekannt() implements Downloadergebnis {}
}
