/**
 * Fachmodell von Standardschreiben (Briefpapier, Standardschreiben, Vorlagenversion, Snapshot, Schreiben). Frei von
 * Frameworks, siehe ADR-002. Kennt keine Domänenservices (ADR-015). Die Bezeichner folgen dem Glossar (arc42 Kapitel
 * 12).
 *
 * <p>Alle Typen sind unveränderlich. Zustandswechsel liefern neue Instanzen.
 */
@NullMarked
package dieterbaier.standardschreiben.domaene.modell;

import org.jspecify.annotations.NullMarked;
