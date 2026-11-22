package dieterbaier.standardschreiben.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.Seite;
import dieterbaier.standardschreiben.domaene.modell.Seitenanfrage;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class UebersichtsergebnisTest {

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void jedesErgebnisIstUnterscheidbar() {
        var seite = new Seite<>(List.of(Testschreiben.SCHREIBEN), 1, new Seitenanfrage(0, 20));

        assertThat(Stream.of(new Uebersichtsergebnis.Gefunden(seite), new Uebersichtsergebnis.NichtErlaubt())
                .map(ergebnis -> switch (ergebnis) {
                    case Uebersichtsergebnis.Gefunden gefunden -> 200;
                    case Uebersichtsergebnis.NichtErlaubt nichtErlaubt -> 403;
                })).containsExactly(200, 403);
    }

    @Test
    void gefundenBrauchtEineSeite() {
        assertThatThrownBy(() -> new Uebersichtsergebnis.Gefunden(null)).hasMessageContaining("Seite");
    }
}
