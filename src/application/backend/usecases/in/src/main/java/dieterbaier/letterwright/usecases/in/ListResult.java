package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.Page;
import dieterbaier.letterwright.domain.model.letter.Letter;
import dieterbaier.letterwright.validation.Validate;

/** Was beim Auflisten der Schreiben zu einem Bezug herauskommt. */
public sealed interface ListResult {

    record Found(Page<Letter> page) implements ListResult {

        public Found {
            Validate.required(page, "Die Seite der Übersicht");
        }
    }

    /** Der Host erlaubt dem Benutzer nicht, die Schreiben zu dem Bezug anzusehen. Gezählt wurde nichts. */
    record NotAllowed() implements ListResult {}
}
