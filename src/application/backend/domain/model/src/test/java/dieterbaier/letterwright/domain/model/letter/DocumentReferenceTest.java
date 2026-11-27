package dieterbaier.letterwright.domain.model.letter;

import static dieterbaier.letterwright.domain.model.Checks.rejects;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DocumentReferenceTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "\t"})
    void theDocumentReferenceRejectsMissingEmptyAndBlankValues(String value) {
        rejects(() -> new DocumentReference(value), "DocumentReference");
    }
}
