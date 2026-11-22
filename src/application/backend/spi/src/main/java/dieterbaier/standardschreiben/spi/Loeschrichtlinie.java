package dieterbaier.standardschreiben.spi;

/**
 * Entscheidet, ob und wie ein Schreiben gelöscht wird. Lösch- und Aufbewahrungsregeln bestimmt der Host (RB-10). Der
 * Host implementiert diese Schnittstelle als Bean.
 *
 * <p>Die Bibliothek fragt erst, nachdem die {@link Berechtigungspruefung} {@link SchreibenAktion#LOESCHEN} erlaubt hat.
 */
public interface Loeschrichtlinie {

    /** Wie das Schreiben gelöscht wird. */
    Loeschentscheidung entscheide(Loeschanfrage anfrage);
}
