package dieterbaier.letterwright.spi;

/**
 * Eine Handlung an den gespeicherten Schreiben zu einem {@link BusinessReference}. Das Erzeugen eines Schreibens ist
 * keine {@code LetterAction}: Es braucht zusätzlich das Standardschreiben, siehe
 * {@link AuthorizationRequest#forCreation(String, BusinessReference, LetterTemplateId)}.
 */
public enum LetterAction {

    /** Die Schreiben in der Übersicht sehen. */
    VIEW,

    /** Ein Schreiben herunterladen. */
    DOWNLOAD,

    /** Ein Schreiben löschen. Wie, entscheidet die {@link DeletionPolicy}. */
    DELETE
}
