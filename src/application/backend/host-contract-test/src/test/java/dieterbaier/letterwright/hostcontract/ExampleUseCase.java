package dieterbaier.letterwright.hostcontract;

import dieterbaier.letterwright.api.SaveLetter;
import dieterbaier.letterwright.api.SaveResult;
import java.util.Optional;
import java.util.UUID;

/**
 * Ein Anwendungsfall des Hosts, der ein Schreiben über die Java-API speichert und es mit eigenen Daten verknüpft
 * (ADR-023). Dass diese Klasse kompiliert, belegt, dass der Host die API sieht und alle Fälle unterscheiden kann.
 */
final class ExampleUseCase {

    private final SaveLetter letter;

    ExampleUseCase(SaveLetter letter) {
        this.letter = letter;
    }

    /** Liefert die ID des Schreibens, mit der der Host die Aufgabe abschließt, oder nichts bei einer Ablehnung. */
    Optional<UUID> completeTask(String user, UUID snapshot) {
        return switch (letter.save(user, snapshot)) {
            case SaveResult.Saved saved -> Optional.of(saved.letter().id());
            case SaveResult.AlreadySaved already -> Optional.of(already.letter().id());
            case SaveResult.SnapshotUnknown unknown -> Optional.empty();
            case SaveResult.SnapshotExpired expired -> Optional.empty();
            case SaveResult.NotAllowed notAllowed -> Optional.empty();
            case SaveResult.DataContextChanged changed -> Optional.empty();
            case SaveResult.NotConformant notConformant -> Optional.empty();
        };
    }
}
