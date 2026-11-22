package dieterbaier.standardschreiben.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SpeicherergebnisTest {

    private final SchreibenId schreiben = SchreibenId.neu();

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void jedesErgebnisIstUnterscheidbar() {
        assertThat(Stream.of(new Speicherergebnis.Gespeichert(schreiben),
                new Speicherergebnis.BereitsGespeichert(schreiben), new Speicherergebnis.SnapshotUnbekannt(),
                new Speicherergebnis.SnapshotAbgelaufen(), new Speicherergebnis.NichtErlaubt(),
                new Speicherergebnis.DatenkontextGeaendert(), new Speicherergebnis.NichtKonform(List.of("Befund")))
                .map(SpeicherergebnisTest::status))
                .containsExactly(201, 200, 404, 410, 403, 409, 422);
    }

    private static int status(Speicherergebnis ergebnis) {
        return switch (ergebnis) {
            case Speicherergebnis.Gespeichert gespeichert -> 201;
            case Speicherergebnis.BereitsGespeichert bereits -> 200;
            case Speicherergebnis.SnapshotUnbekannt unbekannt -> 404;
            case Speicherergebnis.SnapshotAbgelaufen abgelaufen -> 410;
            case Speicherergebnis.NichtErlaubt nichtErlaubt -> 403;
            case Speicherergebnis.DatenkontextGeaendert geaendert -> 409;
            case Speicherergebnis.NichtKonform nichtKonform -> 422;
        };
    }

    @Test
    void pflichtattribute() {
        assertThat(new Speicherergebnis.Gespeichert(schreiben).schreiben()).isEqualTo(schreiben);
        assertThat(new Speicherergebnis.BereitsGespeichert(schreiben).schreiben()).isEqualTo(schreiben);
        assertThat(new Speicherergebnis.NichtKonform(List.of("Befund")).befunde()).containsExactly("Befund");
        assertThatThrownBy(() -> new Speicherergebnis.Gespeichert(null)).hasMessageContaining("gespeicherte");
        assertThatThrownBy(() -> new Speicherergebnis.BereitsGespeichert(null)).hasMessageContaining("bereits");
        assertThatThrownBy(() -> new Speicherergebnis.NichtKonform(null)).hasMessageContaining("Befunde");
    }
}
