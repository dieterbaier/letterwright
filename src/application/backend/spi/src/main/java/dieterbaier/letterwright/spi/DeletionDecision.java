package dieterbaier.letterwright.spi;

/**
 * Wie ein Schreiben gelöscht wird. Lösch- und Aufbewahrungsregeln bestimmt der Host. Erzeugt wird eine Entscheidung mit
 * {@link #hard()}, {@link #logical()} oder {@link #blocked(String)}.
 */
public sealed interface DeletionDecision {

    /** Das Schreiben und sein PDF werden entfernt. */
    static DeletionDecision hard() {
        return new Hard();
    }

    /** Das Schreiben wird als gelöscht markiert und nicht mehr angezeigt; das PDF bleibt erhalten. */
    static DeletionDecision logical() {
        return new Logical();
    }

    /**
     * Das Schreiben darf nicht gelöscht werden, etwa wegen einer Aufbewahrungsfrist.
     *
     * @param reason warum nicht; die Bibliothek zeigt ihn dem Benutzer an
     */
    static DeletionDecision blocked(String reason) {
        return new Blocked(reason);
    }

    /** Siehe {@link #hard()}. */
    record Hard() implements DeletionDecision {
    }

    /** Siehe {@link #logical()}. */
    record Logical() implements DeletionDecision {
    }

    /** Siehe {@link #blocked(String)}. */
    record Blocked(String reason) implements DeletionDecision {

        public Blocked {
            Require.text(reason, "Der Grund der Löschsperre");
        }
    }
}
