package dieterbaier.letterwright.spi;

/**
 * Liefert die Werte für die Platzhalter eines Standardschreibens. Der Host implementiert diese Schnittstelle als Bean.
 *
 * <p>Die Bibliothek ruft sie beim Erzeugen der Vorschau und erneut beim Speichern auf. Ändern sich die Werte dazwischen,
 * verlangt sie eine neue Vorschau (ADR-007). Gleiche Daten sollten deshalb zu gleichen Werten führen.
 */
public interface PlaceholderDataProvider {

    /** Die bereits formatierten Werte. Gibt es keine Werte, ein leerer Datenkontext. */
    DataContext dataContext(DataRequest request);
}
