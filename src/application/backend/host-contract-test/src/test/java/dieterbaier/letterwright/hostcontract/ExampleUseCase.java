package dieterbaier.letterwright.hostcontract;

import dieterbaier.letterwright.api.SaveLetter;
import dieterbaier.letterwright.api.SaveResult;
import java.util.Optional;
import java.util.UUID;

/**
 * A use case of the host that saves a letter through the Java API and links it with its own data (ADR-023). That this
 * class compiles proves that the host sees the API and can distinguish all cases.
 */
final class ExampleUseCase {

    private final SaveLetter letter;

    ExampleUseCase(SaveLetter letter) {
        this.letter = letter;
    }

    /** Returns the ID of the letter with which the host completes the task, or nothing on a rejection. */
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
