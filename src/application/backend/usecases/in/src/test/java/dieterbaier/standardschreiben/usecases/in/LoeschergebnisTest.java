package dieterbaier.standardschreiben.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class LoeschergebnisTest {

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void jedesErgebnisIstUnterscheidbar() {
        assertThat(Stream.of(new Loeschergebnis.Geloescht(), new Loeschergebnis.Gesperrt("Frist"),
                new Loeschergebnis.Unbekannt())
                .map(ergebnis -> switch (ergebnis) {
                    case Loeschergebnis.Geloescht geloescht -> 204;
                    case Loeschergebnis.Gesperrt gesperrt -> 409;
                    case Loeschergebnis.Unbekannt unbekannt -> 404;
                })).containsExactly(204, 409, 404);
    }

    @Test
    void eineSperreNenntIhrenGrund() {
        assertThat(new Loeschergebnis.Gesperrt("Frist").grund()).isEqualTo("Frist");
        assertThatThrownBy(() -> new Loeschergebnis.Gesperrt(" ")).hasMessageContaining("Grund");
    }
}
