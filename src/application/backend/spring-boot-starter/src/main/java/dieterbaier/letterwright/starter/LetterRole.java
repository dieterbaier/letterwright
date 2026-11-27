package dieterbaier.letterwright.starter;

/**
 * The roles of the default authorization check for letters. Creating is a request type of its own in the SPI, but for
 * the configuration a role like the others; that is why the starter has its own vocabulary.
 */
enum LetterRole {
    CREATE,
    VIEW,
    DOWNLOAD,
    DELETE
}
