package dieterbaier.standardschreiben.spi;

import java.time.Instant;
import java.util.UUID;

/**
 * Die stabilen Identifikatoren eines gespeicherten Schreibens, die der {@link NachDemSpeichern Rückruf} erhält
 * (ADR-023). Über die ID verknüpft der Host seine eigenen Daten mit dem Schreiben.
 *
 * @param id die ID des Schreibens, ein technischer Schlüssel, der sich nie ändert
 * @param bezug der fachliche Bezug, zu dem das Schreiben erzeugt wurde
 * @param standardschreiben die Version des Standardschreibens, aus der es entstand
 * @param benutzerkennung der Benutzer, der die Vorschau erzeugt und gespeichert hat, wie ihn die Spring Security des
 *     Hosts kennt
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
