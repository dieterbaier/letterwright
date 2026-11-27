package dieterbaier.letterwright.spi;

/**
 * Wie der Host auf ein neu angelegtes Schreiben antwortet, siehe {@link LetterSaveCallback}. Erzeugt wird ein Ergebnis
 * mit {@link #accepted()} oder {@link #rejected(String)}.
 */
public sealed interface CallbackResult {

    /** Der Host ist einverstanden; Schreiben und Daten des Hosts werden gemeinsam committet. */
    static CallbackResult accepted() {
        return new Accepted();
    }

    /**
     * Der Host lehnt das Speichern ab; die gemeinsame Transaktion wird zurückgerollt.
     *
     * @param reason warum nicht; die Bibliothek zeigt ihn dem Benutzer an. Nicht leer und nicht nur aus Leerraum
     */
    static CallbackResult rejected(String reason) {
        return new Rejected(reason);
    }

    /** Siehe {@link #accepted()}. */
    record Accepted() implements CallbackResult {
    }

    /** Siehe {@link #rejected(String)}. */
    record Rejected(String reason) implements CallbackResult {

        public Rejected {
            Require.text(reason, "Der Grund der Ablehnung");
        }
    }
}
