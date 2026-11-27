package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.Page;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.validation.Validate;

/** The outcome of listing the letters for a reference. */
public sealed interface ListResult {

    record Found(Page<Letter> page) implements ListResult {

        public Found {
            Validate.required(page, "The page of the list");
        }
    }

    /** The host does not allow the user to view the letters for the reference. Nothing was counted. */
    record NotAllowed() implements ListResult {}
}
