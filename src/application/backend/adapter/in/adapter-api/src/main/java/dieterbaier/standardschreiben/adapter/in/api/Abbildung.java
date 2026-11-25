package dieterbaier.standardschreiben.adapter.in.api;

import dieterbaier.standardschreiben.api.FachlicherBezug;
import dieterbaier.standardschreiben.api.GespeichertesSchreiben;
import dieterbaier.standardschreiben.api.Speicherergebnis;
import dieterbaier.standardschreiben.api.Standardschreibenversion;
import dieterbaier.standardschreiben.domaene.modell.BenutzerKennung;
import dieterbaier.standardschreiben.domaene.modell.schreiben.Schreiben;

/**
 * Bildet zwischen dem Domänenmodell und den Typen der API ab (ADR-023). Die Typen tragen dieselben Fachbegriffe; die
 * Typen der Anwendungsfälle sind hier voll qualifiziert, die der API importiert.
 */
final class Abbildung {

    private Abbildung() {
    }

    /**
     * Der Anwendungsfall speichert nur aus einem eigenen Snapshot des Benutzers. Wer ein Schreiben erhält, hat es deshalb
     * auch erzeugt, auch bei {@code BereitsGespeichert}; das Schreiben selbst führt keine Benutzerkennung.
     */
    static Speicherergebnis zurApi(
            dieterbaier.standardschreiben.usecases.in.Speicherergebnis ergebnis, BenutzerKennung benutzer) {
        return switch (ergebnis) {
            case dieterbaier.standardschreiben.usecases.in.Speicherergebnis.Gespeichert gespeichert ->
                    new Speicherergebnis.Gespeichert(zurApi(gespeichert.schreiben(), benutzer));
            case dieterbaier.standardschreiben.usecases.in.Speicherergebnis.BereitsGespeichert bereits ->
                    new Speicherergebnis.BereitsGespeichert(zurApi(bereits.schreiben(), benutzer));
            case dieterbaier.standardschreiben.usecases.in.Speicherergebnis.SnapshotUnbekannt unbekannt ->
                    new Speicherergebnis.SnapshotUnbekannt();
            case dieterbaier.standardschreiben.usecases.in.Speicherergebnis.SnapshotAbgelaufen abgelaufen ->
                    new Speicherergebnis.SnapshotAbgelaufen();
            case dieterbaier.standardschreiben.usecases.in.Speicherergebnis.NichtErlaubt nichtErlaubt ->
                    new Speicherergebnis.NichtErlaubt();
            case dieterbaier.standardschreiben.usecases.in.Speicherergebnis.DatenkontextGeaendert geaendert ->
                    new Speicherergebnis.DatenkontextGeaendert();
            case dieterbaier.standardschreiben.usecases.in.Speicherergebnis.NichtKonform nichtKonform ->
                    new Speicherergebnis.NichtKonform(nichtKonform.befunde());
            // Der Anwendungsfall hinter der Java-API ist ohne Rückruf verdrahtet (ADR-023). Kommt der Fall trotzdem,
            // ist die Verdrahtung falsch; die Ausnahme rollt die Transaktion des Hosts zurück.
            case dieterbaier.standardschreiben.usecases.in.Speicherergebnis.VomHostAbgelehnt abgelehnt ->
                    throw new IllegalStateException("Über die Java-API gibt es keinen Rückruf, der Host hat aber"
                            + " abgelehnt: " + abgelehnt.grund());
        };
    }

    private static GespeichertesSchreiben zurApi(Schreiben schreiben, BenutzerKennung benutzer) {
        return new GespeichertesSchreiben(schreiben.id().wert(),
                new FachlicherBezug(schreiben.bezug().typ(), schreiben.bezug().id()),
                new Standardschreibenversion(schreiben.standardschreiben().vorlage().wert(),
                        schreiben.standardschreiben().nummer().wert()),
                benutzer.wert(), schreiben.erstellt());
    }
}
