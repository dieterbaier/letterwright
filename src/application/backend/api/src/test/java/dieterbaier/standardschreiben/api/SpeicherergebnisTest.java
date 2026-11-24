package dieterbaier.standardschreiben.api;

import static dieterbaier.standardschreiben.api.Pruefungen.lehntAb;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SpeicherergebnisTest {

    private final GespeichertesSchreiben schreiben = new GespeichertesSchreiben(UUID.randomUUID(),
            new FachlicherBezug("Kunde", "4711"), new Standardschreibenversion(UUID.randomUUID(), 1), "erika",
            Instant.parse("2026-11-24T10:00:00Z"));

    /** So unterscheidet ein Host die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void jedesErgebnisIstUnterscheidbar() {
        assertThat(Stream.of(new Speicherergebnis.Gespeichert(schreiben),
                new Speicherergebnis.BereitsGespeichert(schreiben), new Speicherergebnis.SnapshotUnbekannt(),
                new Speicherergebnis.SnapshotAbgelaufen(), new Speicherergebnis.NichtErlaubt(),
                new Speicherergebnis.DatenkontextGeaendert(), new Speicherergebnis.NichtKonform(List.of("Befund")))
                .map(SpeicherergebnisTest::name))
                .containsExactly("gespeichert", "bereits", "unbekannt", "abgelaufen", "verboten", "geaendert",
                        "nicht konform");
    }

    private static String name(Speicherergebnis ergebnis) {
        return switch (ergebnis) {
            case Speicherergebnis.Gespeichert gespeichert -> "gespeichert";
            case Speicherergebnis.BereitsGespeichert bereits -> "bereits";
            case Speicherergebnis.SnapshotUnbekannt unbekannt -> "unbekannt";
            case Speicherergebnis.SnapshotAbgelaufen abgelaufen -> "abgelaufen";
            case Speicherergebnis.NichtErlaubt nichtErlaubt -> "verboten";
            case Speicherergebnis.DatenkontextGeaendert geaendert -> "geaendert";
            case Speicherergebnis.NichtKonform nichtKonform -> "nicht konform";
        };
    }

    @Test
    void pflichtattribute() {
        assertThat(new Speicherergebnis.Gespeichert(schreiben).schreiben()).isEqualTo(schreiben);
        assertThat(new Speicherergebnis.BereitsGespeichert(schreiben).schreiben()).isEqualTo(schreiben);
        lehntAb(() -> new Speicherergebnis.Gespeichert(null), "Das gespeicherte Schreiben");
        lehntAb(() -> new Speicherergebnis.BereitsGespeichert(null), "Das bereits gespeicherte Schreiben");
    }

    @Test
    void dieBefundeSindEineUnveraenderlicheKopieOhneLuecken() {
        List<String> befunde = new ArrayList<>(List.of("PDF/UA-1: Bild ohne Alternativtext"));
        var nichtKonform = new Speicherergebnis.NichtKonform(befunde);
        befunde.clear();

        assertThat(nichtKonform.befunde()).containsExactly("PDF/UA-1: Bild ohne Alternativtext");
        assertThatThrownBy(() -> nichtKonform.befunde().add("weiterer"))
                .isInstanceOf(UnsupportedOperationException.class);
        lehntAb(() -> new Speicherergebnis.NichtKonform(null), "Die Befunde");
        lehntAb(() -> new Speicherergebnis.NichtKonform(Arrays.asList("Befund", null)), "Die Befunde");
    }
}
