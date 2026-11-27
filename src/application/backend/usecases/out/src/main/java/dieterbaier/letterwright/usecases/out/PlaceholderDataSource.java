package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.UserId;
import dieterbaier.letterwright.domain.model.DataContext;
import dieterbaier.letterwright.domain.model.BusinessReference;
import dieterbaier.letterwright.domain.model.template.LetterTemplate;

/** Provides the host's data context in domain terms. Implemented by {@code adapter-host} (ADR-016). */
public interface PlaceholderDataSource {

    /**
     * Reads the already formatted values for a letter template and a business reference.
     *
     * @throws IllegalStateException if the host does not provide a valid data context
     */
    DataContext readDataContext(UserId user, BusinessReference reference, LetterTemplate letterTemplate);
}
