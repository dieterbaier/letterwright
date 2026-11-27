package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.validation.Validate;
import java.time.Instant;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * A letter is visible until it is logically deleted. A logically deleted letter keeps its PDF but appears in no list
 * and can neither be downloaded nor deleted again; for the user it no longer exists. A hard deleted letter has no
 * state; it is removed.
 */
@ValueObject
public sealed interface LetterState {

    record Visible() implements LetterState {}

    record LogicallyDeleted(Instant at) implements LetterState {

        public LogicallyDeleted {
            Validate.required(at, "The time of deletion");
        }
    }
}
