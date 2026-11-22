package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.validierung.Pruefe;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;
import java.util.List;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Eine Seite einer Übersicht. Einträge und Gesamtzahl beziehen sich auf dieselbe, schon gefilterte Menge: Was der
 * Benutzer nicht sehen darf, zählt nicht mit (QS-009).
 *
 * <p>Eine Seite hinter dem Ende ist leer, aber gültig, etwa wenn seit dem letzten Abruf Schreiben gelöscht wurden.
 */
@ValueObject
public record Seite<T>(List<T> eintraege, long gesamtzahl, Seitenanfrage anfrage) {

    public Seite {
        eintraege = List.copyOf(Pruefe.pflichtOhneLuecken(eintraege, "Die Einträge der Seite"));
        Pruefe.pflicht(anfrage, "Die Seitenanfrage");
        if (eintraege.size() > anfrage.groesse()) {
            throw new Validierungsfehler("Die Seite hat " + eintraege.size() + " Einträge, höchstens "
                    + anfrage.groesse() + " sind erlaubt");
        }
        if (gesamtzahl < 0) {
            throw new Validierungsfehler("Die Gesamtzahl darf nicht negativ sein, war " + gesamtzahl);
        }
        if (!eintraege.isEmpty() && gesamtzahl < anfrage.versatz() + eintraege.size()) {
            throw new Validierungsfehler("Die Gesamtzahl " + gesamtzahl + " ist kleiner als die Einträge bis "
                    + "einschließlich dieser Seite");
        }
    }

    /** Wie viele Seiten dieser Größe die Gesamtzahl füllt; ohne Einträge keine. */
    public long anzahlSeiten() {
        return (gesamtzahl + anfrage.groesse() - 1) / anfrage.groesse();
    }
}
