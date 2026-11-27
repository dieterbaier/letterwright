package dieterbaier.letterwright.domain.model;

import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import java.util.List;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Eine Seite einer Übersicht. Einträge und Gesamtzahl beziehen sich auf dieselbe, schon gefilterte Menge: Was der
 * Benutzer nicht sehen darf, zählt nicht mit (QS-009).
 *
 * <p>Eine Seite hinter dem Ende ist leer, aber gültig, etwa wenn seit dem letzten Abruf Schreiben gelöscht wurden.
 */
@ValueObject
public record Page<T>(List<T> entries, long total, PageRequest request) {

    public Page {
        entries = List.copyOf(Validate.requiredWithoutGaps(entries, "Die Einträge der Seite"));
        Validate.required(request, "Die Seitenanfrage");
        if (entries.size() > request.size()) {
            throw new ValidationException("Die Seite hat " + entries.size() + " Einträge, höchstens "
                    + request.size() + " sind erlaubt");
        }
        if (total < 0) {
            throw new ValidationException("Die Gesamtzahl darf nicht negativ sein, war " + total);
        }
        if (!entries.isEmpty() && total < request.offset() + entries.size()) {
            throw new ValidationException("Die Gesamtzahl " + total + " ist kleiner als die Einträge bis "
                    + "einschließlich dieser Seite");
        }
    }

    /** Wie viele Seiten dieser Größe die Gesamtzahl füllt; ohne Einträge keine. */
    public long pageCount() {
        return (total + request.size() - 1) / request.size();
    }
}
