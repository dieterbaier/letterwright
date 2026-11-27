package dieterbaier.letterwright.domain.model.letter;

import dieterbaier.letterwright.validation.Validate;
import org.jmolecules.ddd.annotation.ValueObject;

/** Reference to the PDF of a letter in the document store. Only the store knows what it means. */
@ValueObject
public record DocumentReference(String value) {

    public DocumentReference {
        Validate.notBlank(value, "The DocumentReference");
    }
}
