package dieterbaier.standardschreiben.hostvertrag;

import dieterbaier.standardschreiben.api.SchreibenSpeichern;
import dieterbaier.standardschreiben.api.Speicherergebnis;
import java.util.Optional;
import java.util.UUID;

/**
 * Ein Anwendungsfall des Hosts, der ein Schreiben über die Java-API speichert und es mit eigenen Daten verknüpft
 * (ADR-023). Dass diese Klasse kompiliert, belegt, dass der Host die API sieht und alle Fälle unterscheiden kann.
 */
final class BeispielAnwendungsfall {

    private final SchreibenSpeichern schreiben;

    BeispielAnwendungsfall(SchreibenSpeichern schreiben) {
        this.schreiben = schreiben;
    }

    /** Liefert die ID des Schreibens, mit der der Host die Aufgabe abschließt, oder nichts bei einer Ablehnung. */
    Optional<UUID> schliesseAufgabeAb(String benutzer, UUID snapshot) {
        return switch (schreiben.speichere(benutzer, snapshot)) {
            case Speicherergebnis.Gespeichert gespeichert -> Optional.of(gespeichert.schreiben().id());
            case Speicherergebnis.BereitsGespeichert bereits -> Optional.of(bereits.schreiben().id());
            case Speicherergebnis.SnapshotUnbekannt unbekannt -> Optional.empty();
            case Speicherergebnis.SnapshotAbgelaufen abgelaufen -> Optional.empty();
            case Speicherergebnis.NichtErlaubt nichtErlaubt -> Optional.empty();
            case Speicherergebnis.DatenkontextGeaendert geaendert -> Optional.empty();
            case Speicherergebnis.NichtKonform nichtKonform -> Optional.empty();
        };
    }
}
