package dieterbaier.letterwright.usecases.out;

import dieterbaier.letterwright.domain.model.letter.DocumentReference;

/** Legt das PDF eines Schreibens ab. Was ein Dokumentverweis bedeutet, weiß nur die Ablage. */
public interface DocumentStore {

    DocumentReference put(byte[] pdf);

    /**
     * Die Bytes, wie sie abgelegt wurden.
     *
     * @throws IllegalStateException wenn es zum Verweis kein PDF gibt
     */
    byte[] read(DocumentReference document);

    /** Für ein PDF, auf das kein Schreiben (mehr) verweist: nie gespeichert oder hart gelöscht. */
    void remove(DocumentReference document);
}
