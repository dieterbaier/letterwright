package dieterbaier.standardschreiben.spi;

/** Eine Handlung an den Schreiben zu einem {@link FachlicherBezug}. */
public enum SchreibenAktion implements Aktion {

    /** Eine Vorschau erzeugen und ein Schreiben speichern. */
    ERZEUGEN,

    /** Die Schreiben in der Übersicht sehen. */
    ANSEHEN,

    /** Ein Schreiben herunterladen. */
    HERUNTERLADEN,

    /** Ein Schreiben löschen. Wie, entscheidet die {@link Loeschrichtlinie}. */
    LOESCHEN
}
