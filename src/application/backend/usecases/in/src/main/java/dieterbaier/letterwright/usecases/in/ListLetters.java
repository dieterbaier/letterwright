package dieterbaier.letterwright.usecases.in;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.PageRequest;

/**
 * Use case "list letters": the visible letters for a business reference, page by page. What the user may not see
 * counts neither towards the page nor towards the total (QS-009).
 */
public interface ListLetters {

    ListResult list(UserId user, BusinessReference reference, PageRequest page);
}
