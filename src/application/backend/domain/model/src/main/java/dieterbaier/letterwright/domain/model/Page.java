package dieterbaier.letterwright.domain.model;

import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import java.util.List;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * A page of a list. Entries and total refer to the same, already filtered set: what the user may not see is not
 * counted (QS-009).
 *
 * <p>A page past the end is empty but valid, for example when letters were deleted since the last request.
 */
@ValueObject
public record Page<T>(List<T> entries, long total, PageRequest request) {

    public Page {
        entries = List.copyOf(Validate.requiredWithoutGaps(entries, "The entries of the page"));
        Validate.required(request, "The PageRequest");
        if (entries.size() > request.size()) {
            throw new ValidationException("The page has " + entries.size() + " entries, at most "
                    + request.size() + " are allowed");
        }
        if (total < 0) {
            throw new ValidationException("The total must not be negative, was " + total);
        }
        if (!entries.isEmpty() && total < request.offset() + entries.size()) {
            throw new ValidationException("The total " + total + " is less than the entries up to "
                    + "and including this page");
        }
    }

    /** How many pages of this size the total fills; none without entries. */
    public long pageCount() {
        return (total + request.size() - 1) / request.size();
    }
}
