package dieterbaier.standardschreiben.spi;

/**
 * Eine Handlung an den gespeicherten Schreiben zu einem {@link FachlicherBezug}. Das Erzeugen eines Schreibens ist
 * keine {@code SchreibenAktion}: Es braucht zusätzlich das Standardschreiben, siehe
 * {@link Berechtigungsanfrage#zumErzeugen(String, FachlicherBezug, Standardschreibenkennung)}.
 */
public enum SchreibenAktion {

    /** Die Schreiben in der Übersicht sehen. */
    ANSEHEN,

    /** Ein Schreiben herunterladen. */
    HERUNTERLADEN,

    /** Ein Schreiben löschen. Wie, entscheidet die {@link Loeschrichtlinie}. */
    LOESCHEN
}
