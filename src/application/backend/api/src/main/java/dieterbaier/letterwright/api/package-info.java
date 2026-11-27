/**
 * Java-API für die Host-Anwendung, siehe ADR-023: Anwendungsfälle, die der Host in seinem eigenen Code und in seiner
 * eigenen Transaktion aufruft, und die Datentypen, die über sie fließen. Hängt wie das SPI an keinem anderen Modul der
 * Bibliothek und nicht an Spring (Muster aus ADR-016). Den Rückruf beim Speichern über die Oberfläche beschreibt das
 * SPI, nicht dieses Paket.
 *
 * <p>Heute gibt es einen Anwendungsfall: {@link dieterbaier.letterwright.api.SaveLetter}.
 *
 * <p>Für alle Typen dieses Pakets gilt:
 * <ul>
 *   <li>Kein Parameter und kein Rückgabewert ist {@code null}. Das Paket ist {@code @NullMarked} (JSpecify); wo ein Wert
 *       fehlen darf, sagt es der Typ.</li>
 *   <li>Alle Datentypen sind unveränderlich und prüfen sich beim Erzeugen. Ein fehlender oder ungültiger Wert ergibt
 *       eine {@link IllegalArgumentException} mit dem Namen des Attributs.</li>
 *   <li>Fachliche Ablehnungen sind Fälle des Ergebnistyps, keine Ausnahmen. Eine Ausnahme ist ein technischer Fehler.</li>
 * </ul>
 */
@NullMarked
package dieterbaier.letterwright.api;

import org.jspecify.annotations.NullMarked;
