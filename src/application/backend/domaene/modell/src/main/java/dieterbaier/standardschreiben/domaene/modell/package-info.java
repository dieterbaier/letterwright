/**
 * Fachmodell von Standardschreiben. Frei von Frameworks, siehe ADR-002. Kennt keine Domänenservices (ADR-015). Die
 * Bezeichner folgen dem Glossar (arc42 Kapitel 12).
 *
 * <p>Je Aggregat gibt es ein Unterpaket: {@code vorlage} (Briefpapier und Standardschreiben), {@code snapshot} und
 * {@code schreiben}. Was nur ein Aggregat braucht, liegt in dessen Paket und ist dort so weit wie möglich gekapselt.
 * Dieses Paket enthält die Werttypen, die mehrere Aggregate und die Ports teilen; es kennt keines der Unterpakete.
 *
 * <p>Alle Typen sind unveränderlich. Zustandswechsel liefern neue Instanzen.
 */
@NullMarked
package dieterbaier.standardschreiben.domaene.modell;

import org.jspecify.annotations.NullMarked;
