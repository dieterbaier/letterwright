package dieterbaier.standardschreiben.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Die stabilen Identifikatoren eines gespeicherten Schreibens (ADR-023). Über die ID verknüpft der Host seine eigenen
 * Daten mit dem Schreiben.
 *
 * @param id die ID des Schreibens, ein technischer Schlüssel, der sich nie ändert
 * @param bezug der fachliche Bezug, zu dem das Schreiben erzeugt wurde
 * @param standardschreiben die Version des Standardschreibens, aus der es entstand
 * @param benutzerkennung der Benutzer, der die Vorschau erzeugt und gespeichert hat; nicht leer und nicht nur aus
 *     Leerraum
 * @param erstellt der Zeitpunkt der Erstellung
 */
public record GespeichertesSchreiben(
        UUID id, FachlicherBezug bezug, Standardschreibenversion standardschreiben, String benutzerkennung,
        Instant erstellt) {

    public GespeichertesSchreiben {
        Pflicht.wert(id, "Die ID des Schreibens");
        Pflicht.wert(bezug, "Der fachliche Bezug");
        Pflicht.wert(standardschreiben, "Das Standardschreiben");
        Pflicht.text(benutzerkennung, "Die Benutzerkennung");
        Pflicht.wert(erstellt, "Der Zeitpunkt der Erstellung");
    }
}
