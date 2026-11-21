/**
 * SPI zur Host-Anwendung: Schnittstellen, die der Host implementiert, und die Datentypen, die über
 * sie fließen, siehe ADR-016. Hängt an keinem anderen Modul der Bibliothek und nicht an Spring.
 *
 * <p>Der Host implementiert drei Schnittstellen: {@link dieterbaier.standardschreiben.spi.PlatzhalterdatenAnbieter},
 * {@link dieterbaier.standardschreiben.spi.Berechtigungspruefung} (optional, der Starter bringt eine Default-Implementierung
 * mit) und {@link dieterbaier.standardschreiben.spi.Loeschrichtlinie}. Alle Datentypen sind unveränderlich. Ein fehlender
 * oder ungültiger Wert ergibt eine {@link IllegalArgumentException} mit dem Namen des Attributs.
 */
@NullMarked
package dieterbaier.standardschreiben.spi;

import org.jspecify.annotations.NullMarked;
