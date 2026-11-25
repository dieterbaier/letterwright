package dieterbaier.standardschreiben.spi;

/**
 * Optionaler Rückruf, wenn ein Benutzer über die Oberfläche der Bibliothek ein Schreiben speichert (ADR-023). Der Host
 * ändert darin eigene Daten im selben Commit wie das Schreiben, oder er lehnt das Speichern mit einem Grund ab. Der Host
 * implementiert diese Schnittstelle als Bean, wenn er sie braucht; ohne Bean verhält sich die Bibliothek wie ohne
 * Rückruf. Es gibt höchstens einen Rückruf. Stellt der Host mehrere Beans bereit, wählt Spring nach seinen üblichen
 * Regeln eine aus, etwa die mit {@code @Primary}; nur sie wird gerufen. Bleibt die Auswahl mehrdeutig, startet die
 * Anwendung nicht.
 *
 * <p><b>Wann:</b> Die Bibliothek ruft {@link #angelegt} in derselben Transaktion wie das Speichern auf, nach ihren
 * eigenen Datenbankänderungen und vor dem Commit. Einen Aufruf nach dem Commit gibt es nicht. Die Datenbankaktionen des
 * Hosts nehmen an dieser Transaktion teil, wenn er denselben Transaktionsmanager wie die Bibliothek benutzt.
 *
 * <p><b>Nur bei einer Neuanlage:</b> Der Rückruf läuft nur, wenn der aktuelle Aufruf das Schreiben anlegt. Speichert
 * der Benutzer dieselbe Vorschau erneut, etwa nach einer verlorenen Antwort, entsteht kein neues Schreiben und es gibt
 * keinen Rückruf. Mehr sichert der Vertrag nicht zu: Ein vorhandenes Schreiben kann über die Java-API ohne Rückruf
 * entstanden sein. Über die Java-API gibt es nie einen Rückruf; dort wertet der Host das Ergebnis selbst aus.
 *
 * <p><b>Ablehnen:</b> {@link Rueckrufergebnis#abgelehnt(String)} rollt die gemeinsame Transaktion zurück. Danach ist
 * nichts gespeichert, weder das Schreiben noch die Daten des Hosts, und der Benutzer sieht den Grund. Die Vorschau
 * bleibt gültig, bis sie abläuft; der Benutzer kann erneut speichern.
 *
 * <p><b>Ausnahmen:</b> Jede Ausnahme ist ein technischer Fehler und rollt die gemeinsame Transaktion ebenfalls zurück.
 * Die Bibliothek wiederholt nicht; eine Wiederholung ist ein neuer Speicherversuch des Benutzers.
 *
 * <p><b>Wirkungen außerhalb der Datenbank</b>, etwa der Abschluss eines Tasks in einer entfernten Engine, gehören nicht
 * in den Rückruf. Der Host schreibt dafür einen Auftrag in seine Outbox in derselben Transaktion und führt ihn nach dem
 * Commit wiederholbar und idempotent selbst aus.
 */
public interface NachDemSpeichern {

    /**
     * Ein Benutzer hat über die Oberfläche ein Schreiben angelegt; die Transaktion ist noch nicht committet.
     *
     * @param schreiben die stabilen Identifikatoren des neuen Schreibens; über die ID verknüpft der Host seine Daten
     * @return {@link Rueckrufergebnis#angenommen()}, damit das Schreiben committet wird, oder
     *     {@link Rueckrufergebnis#abgelehnt(String)} mit einem Grund für den Benutzer
     */
    Rueckrufergebnis angelegt(GespeichertesSchreiben schreiben);
}
