/**
 * Allgemeine Prüfungen, mit denen selbstvalidierende Typen ihre Attribute im Konstruktor prüfen. Welche Regel für ein
 * Attribut gilt, entscheidet der Typ selbst; fachliche Regeln wie Zustandsübergänge gehören nicht hierher.
 *
 * <p>Fehlervertrag: Jede verletzte Prüfung wirft einen {@link dieterbaier.standardschreiben.validierung.Validierungsfehler}
 * mit der Bezeichnung des Attributs, nie eine {@link NullPointerException}. Frei von Frameworks und ohne Abhängigkeit zu
 * anderen Modulen. Das SPI nutzt dieses Modul nicht (ADR-016).
 */
@NullMarked
package dieterbaier.standardschreiben.validierung;

import org.jspecify.annotations.NullMarked;
