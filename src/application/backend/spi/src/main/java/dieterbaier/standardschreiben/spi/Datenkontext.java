package dieterbaier.standardschreiben.spi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Die Werte, die der Host für ein Standardschreiben und einen fachlichen Bezug liefert.
 *
 * <p>Die Werte sind bereits formatiert (Beträge, Datum; ADR-008). Erlaubt sind Texte ({@link String}), Listen und Maps
 * mit Text-Schlüsseln, beliebig verschachtelt. Ein fehlender Wert wird weggelassen, nicht als {@code null} geliefert.
 * Ob die Werte diese Regeln erfüllen, prüft die Bibliothek beim Übernehmen.
 */
public record Datenkontext(Map<String, ?> werte) {

    public Datenkontext {
        werte = Collections.unmodifiableMap(new LinkedHashMap<>(Pflicht.wert(werte, "Die Werte des Datenkontexts")));
    }
}
