package dieterbaier.standardschreiben.api;

import java.util.UUID;

/**
 * Anwendungsfall „Schreiben speichern“ für den Code des Hosts (ADR-023). Der Host ruft ihn in seinem eigenen
 * Anwendungsfall auf, wertet das Ergebnis aus und committet seine eigenen Datenbankänderungen gemeinsam mit dem
 * Schreiben. Die Implementierung stellt der Starter bereit.
 *
 * <p>Es gelten dieselben Regeln wie beim Speichern über die Oberfläche: dieselbe Berechtigungsprüfung, dasselbe PDF aus
 * dem Snapshot der Vorschau, gespeichert nur, wenn es konform ist, und höchstens ein Schreiben je Snapshot (ADR-021).
 * Einen Rückruf an den Host gibt es auf diesem Weg nicht.
 *
 * <p><b>Transaktion:</b> Der Aufruf tritt einer laufenden Transaktion bei und eröffnet nur dann eine eigene, wenn keine
 * läuft (Spring {@code REQUIRED}); eine unabhängige Transaktion eröffnet er nie. Atomar mit den eigenen Aktionen ist das
 * Speichern also nur, wenn der Host die Transaktion vor dem Aufruf eröffnet, über denselben Transaktionsmanager wie die
 * Bibliothek. Rollt der Host zurück, rollen Schreiben und Verbrauch des Snapshots mit zurück; das abgelegte PDF bleibt
 * dann ohne Schreiben zurück.
 *
 * <p>Eine fachliche Ablehnung ist ein Fall von {@link Speicherergebnis} und markiert die Transaktion nicht zum
 * Zurückrollen. Jede Ausnahme aus diesem Aufruf markiert eine laufende Transaktion dagegen zum Zurückrollen, auch wenn
 * der Host sie fängt: ein technischer Fehler ebenso wie eine {@link IllegalArgumentException} für eine ungültige
 * Eingabe, also einen Fehler des Aufrufers.
 */
public interface SchreibenSpeichern {

    /**
     * Speichert das Schreiben aus dem Snapshot einer Vorschau.
     *
     * @param benutzerkennung der angemeldete Benutzer, wie ihn die Spring Security des Hosts kennt; derselbe, der die
     *     Vorschau erzeugt hat, sonst ist der Snapshot unbekannt. Nicht leer und nicht nur aus Leerraum
     * @param snapshot die ID des Snapshots, die der Host mit der Vorschau erhalten hat
     * @return das Ergebnis; bei einer Ablehnung ist nichts gespeichert
     */
    Speicherergebnis speichere(String benutzerkennung, UUID snapshot);
}
