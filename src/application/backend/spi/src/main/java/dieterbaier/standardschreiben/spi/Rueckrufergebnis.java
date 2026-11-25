package dieterbaier.standardschreiben.spi;

/**
 * Wie der Host auf ein neu angelegtes Schreiben antwortet, siehe {@link NachDemSpeichern}. Erzeugt wird ein Ergebnis
 * mit {@link #angenommen()} oder {@link #abgelehnt(String)}.
 */
public sealed interface Rueckrufergebnis {

    /** Der Host ist einverstanden; Schreiben und Daten des Hosts werden gemeinsam committet. */
    static Rueckrufergebnis angenommen() {
        return new Angenommen();
    }

    /**
     * Der Host lehnt das Speichern ab; die gemeinsame Transaktion wird zurückgerollt.
     *
     * @param grund warum nicht; die Bibliothek zeigt ihn dem Benutzer an. Nicht leer und nicht nur aus Leerraum
     */
    static Rueckrufergebnis abgelehnt(String grund) {
        return new Abgelehnt(grund);
    }

    /** Siehe {@link #angenommen()}. */
    record Angenommen() implements Rueckrufergebnis {
    }

    /** Siehe {@link #abgelehnt(String)}. */
    record Abgelehnt(String grund) implements Rueckrufergebnis {

        public Abgelehnt {
            Pflicht.text(grund, "Der Grund der Ablehnung");
        }
    }
}
