package dieterbaier.standardschreiben.domaene.modell;

/** Gemeinsame Prüfungen für die Werttypen des Modells. */
final class Pruefe {

    private Pruefe() {
    }

    /** Liefert den Text unverändert, wenn er nicht leer ist und nicht nur aus Leerraum besteht. */
    static String nichtLeer(String text, String bezeichnung) {
        if (text.isBlank()) {
            throw new IllegalArgumentException(bezeichnung + " darf nicht leer sein");
        }
        return text;
    }
}
