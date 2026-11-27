package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.PageRequest;

/**
 * Anwendungsfall „Schreiben auflisten“: die sichtbaren Schreiben zu einem fachlichen Bezug, seitenweise. Was der
 * Benutzer nicht sehen darf, zählt weder in die Seite noch in die Gesamtzahl (QS-009).
 */
public interface ListLetters {

    ListResult list(UserId user, BusinessReference reference, PageRequest page);
}
