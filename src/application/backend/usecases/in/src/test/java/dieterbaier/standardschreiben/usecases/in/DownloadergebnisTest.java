package dieterbaier.standardschreiben.usecases.in;

import static dieterbaier.standardschreiben.usecases.in.Testschreiben.PDF;
import static dieterbaier.standardschreiben.usecases.in.Testschreiben.SCHREIBEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class DownloadergebnisTest {

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void jedesErgebnisIstUnterscheidbar() {
        assertThat(Stream.of(new Downloadergebnis.Geliefert(SCHREIBEN, PDF.clone()), new Downloadergebnis.Unbekannt())
                .map(ergebnis -> switch (ergebnis) {
                    case Downloadergebnis.Geliefert geliefert -> 200;
                    case Downloadergebnis.Unbekannt unbekannt -> 404;
                })).containsExactly(200, 404);
    }

    @Test
    void dasGeliefertePdfLaesstSichNichtVonAussenAendern() {
        byte[] pdf = PDF.clone();
        var geliefert = new Downloadergebnis.Geliefert(SCHREIBEN, pdf);
        pdf[0] = 9;
        geliefert.pdf()[1] = 9;

        assertThat(geliefert.pdf()).containsExactly(PDF);
        assertThat(geliefert.schreiben()).isEqualTo(SCHREIBEN);
    }

    @Test
    void pflichtattribute() {
        assertThatThrownBy(() -> new Downloadergebnis.Geliefert(null, PDF)).hasMessageContaining("Schreiben");
        assertThatThrownBy(() -> new Downloadergebnis.Geliefert(SCHREIBEN, null)).hasMessageContaining("PDF");
    }
}
