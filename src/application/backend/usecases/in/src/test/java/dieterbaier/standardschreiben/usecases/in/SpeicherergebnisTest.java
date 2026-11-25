package dieterbaier.standardschreiben.usecases.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dieterbaier.standardschreiben.domaene.modell.FachlicherBezug;
import dieterbaier.standardschreiben.domaene.modell.Pruefsumme;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Dokumentverweis;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;
import dieterbaier.standardschreiben.domaene.modell.schreiben.SchreibenId;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreibenzustand;
import dieterbaier.standardschreiben.domaene.modell.snapshot.SnapshotId;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsnummer;
import dieterbaier.standardschreiben.domaene.modell.vorlage.Versionsverweis;
import dieterbaier.standardschreiben.domaene.modell.vorlage.VorlagenId;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SpeicherergebnisTest {

    private final Schreiben schreiben = Schreiben.wiederherstellen(SchreibenId.neu(), SnapshotId.neu(),
            new FachlicherBezug("Kunde", "4711"), new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE),
            new Versionsverweis(VorlagenId.neu(), Versionsnummer.ERSTE), new Dokumentverweis("ablage/1"),
            Pruefsumme.ueber(new byte[] {1}), Instant.parse("2026-11-24T10:00:00Z"), new Schreibenzustand.Sichtbar());

    /** So unterscheidet ein eingehender Adapter die Ergebnisse; der Compiler prüft, dass keines fehlt. */
    @Test
    void jedesErgebnisIstUnterscheidbar() {
        assertThat(Stream.of(new Speicherergebnis.Gespeichert(schreiben),
                new Speicherergebnis.BereitsGespeichert(schreiben), new Speicherergebnis.SnapshotUnbekannt(),
                new Speicherergebnis.SnapshotAbgelaufen(), new Speicherergebnis.NichtErlaubt(),
                new Speicherergebnis.DatenkontextGeaendert(), new Speicherergebnis.NichtKonform(List.of("Befund")),
                new Speicherergebnis.VomHostAbgelehnt("Aufgabe ist bereits erledigt"))
                .map(SpeicherergebnisTest::status))
                .containsExactly(201, 200, 404, 410, 403, 409, 422, 400);
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
            case Speicherergebnis.VomHostAbgelehnt abgelehnt -> 400;
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
        assertThat(new Speicherergebnis.VomHostAbgelehnt("Aufgabe ist bereits erledigt").grund())
                .isEqualTo("Aufgabe ist bereits erledigt");
        assertThatThrownBy(() -> new Speicherergebnis.VomHostAbgelehnt(" ")).hasMessageContaining("Grund");
    }
}
