package dieterbaier.standardschreiben.spi;

/**
 * Wie ein Schreiben gelöscht wird. Lösch- und Aufbewahrungsregeln bestimmt der Host. Erzeugt wird eine Entscheidung mit
 * {@link #hart()}, {@link #logisch()} oder {@link #gesperrt(String)}.
 */
public sealed interface Loeschentscheidung {

    /** Das Schreiben und sein PDF werden entfernt. */
    static Loeschentscheidung hart() {
        return new Hart();
    }

    /** Das Schreiben wird als gelöscht markiert und nicht mehr angezeigt; das PDF bleibt erhalten. */
    static Loeschentscheidung logisch() {
        return new Logisch();
    }

    /**
     * Das Schreiben darf nicht gelöscht werden, etwa wegen einer Aufbewahrungsfrist.
     *
     * @param grund warum nicht; die Bibliothek zeigt ihn dem Benutzer an
     */
    static Loeschentscheidung gesperrt(String grund) {
        return new Gesperrt(grund);
    }

    /** Siehe {@link #hart()}. */
    record Hart() implements Loeschentscheidung {
    }

    /** Siehe {@link #logisch()}. */
    record Logisch() implements Loeschentscheidung {
    }

    /** Siehe {@link #gesperrt(String)}. */
    record Gesperrt(String grund) implements Loeschentscheidung {

        public Gesperrt {
            Pflicht.text(grund, "Der Grund der Löschsperre");
        }
    }
}
