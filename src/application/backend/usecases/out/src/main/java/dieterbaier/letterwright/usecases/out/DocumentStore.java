package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.letter.DocumentReference;

/** Stores the PDF of a letter. Only the store knows what a document reference means. */
public interface DocumentStore {

    DocumentReference put(byte[] pdf);

    /**
     * The bytes as they were stored.
     *
     * @throws IllegalStateException if there is no PDF for the reference
     */
    byte[] read(DocumentReference document);

    /** For a PDF that no letter refers to (any more): never saved or hard deleted. */
    void remove(DocumentReference document);
}
