/**
 * SPI zur Host-Anwendung: Schnittstellen, die der Host implementiert, und die Datentypen, die über
 * sie fließen, siehe ADR-016. Hängt an keinem anderen Modul der Bibliothek und nicht an Spring.
 *
 * <p>Der Host implementiert drei Schnittstellen: {@link dieterbaier.standardschreiben.spi.PlatzhalterdatenAnbieter},
 * {@link dieterbaier.standardschreiben.spi.Berechtigungspruefung} (optional, der Starter bringt eine Default-Implementierung
 * mit) und {@link dieterbaier.standardschreiben.spi.Loeschrichtlinie}.
 *
 * <p>Für alle Typen dieses Pakets gilt:
 * <ul>
 *   <li>Kein Parameter und kein Rückgabewert ist {@code null}. Das Paket ist {@code @NullMarked} (JSpecify); wo ein Wert
 *       fehlen darf, sagt es der Typ. Liefert eine Implementierung des Hosts {@code null}, bricht die Bibliothek mit
 *       einer Vertragsverletzung ab.</li>
 *   <li>Alle Datentypen sind unveränderlich und prüfen sich beim Erzeugen. Ein fehlender oder ungültiger Wert ergibt
 *       eine {@link IllegalArgumentException} mit dem Namen des Attributs.</li>
 * </ul>
 */
@NullMarked
package dieterbaier.standardschreiben.spi;

import org.jspecify.annotations.NullMarked;
