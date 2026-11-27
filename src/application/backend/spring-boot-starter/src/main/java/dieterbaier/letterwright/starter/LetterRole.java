package dieterbaier.letterwright.starter;

/**
 * Die Rollen der Default-Berechtigungsprüfung für Schreiben. Das Erzeugen ist im SPI eine eigene Anfrageart, für die
 * Konfiguration aber eine Rolle wie die anderen; deshalb hat der Starter sein eigenes Vokabular.
 */
enum LetterRole {
    CREATE,
    VIEW,
    DOWNLOAD,
    DELETE
}
