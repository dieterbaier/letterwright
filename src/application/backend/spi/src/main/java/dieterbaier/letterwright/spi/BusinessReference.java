package dieterbaier.letterwright.spi;

/**
 * Das Domänenobjekt des Hosts, zu dem ein Schreiben erzeugt wird, als Typ und ID (z. B. {@code Kunde}, {@code 4711}).
 * Die Bibliothek deutet beides nicht, sie reicht es nur weiter.
 *
 * <p>Typ und ID zusammen bezeichnen genau ein Objekt des Hosts. Braucht der Host mehrere Schlüssel, kodiert er sie
 * selbst in der ID und zerlegt sie wieder. Die Bibliothek vergleicht Bezüge per Gleichheit und speichert sie mit jedem
 * Schreiben. Die ID muss deshalb
 *
 * <ul>
 *   <li>eindeutig sein: Verschiedene Objekte desselben Bezugstyps erhalten verschiedene IDs. Eine Kodierung mehrerer
 *       Schlüssel muss daher kollisionsfrei und eindeutig dekodierbar sein,
 *   <li>kanonisch sein: dasselbe Objekt ergibt immer dieselbe Zeichenkette,
 *   <li>dauerhaft stabil sein: keine fachlichen Werte, die sich ändern können,
 *   <li>ohne Schaden sichtbar sein: Sie kommt vom Browser, der Host prüft sie und gibt keine vertraulichen Daten darin
 *       preis.
 * </ul>
 *
 * <p>Bevor der Host mehrere Schlüssel in eine ID kodiert, prüft er, ob der Bezug richtig geschnitten ist: Oft passt
 * ein eigener Bezugstyp mit eigener ID besser.
 *
 * @param type der Bezugstyp des Hosts, z. B. {@code Kunde}; nicht leer und nicht nur aus Leerraum
 * @param id die ID des Objekts nach dem obigen Vertrag; nicht leer und nicht nur aus Leerraum
 */
public record BusinessReference(String type, String id) {

    public BusinessReference {
        Require.text(type, "Der Typ des fachlichen Bezugs");
        Require.text(id, "Die ID des fachlichen Bezugs");
    }
}
