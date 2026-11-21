package dieterbaier.standardschreiben.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Die Werte, die der Host für ein Standardschreiben und einen fachlichen Bezug liefert.
 *
 * <p>Die Werte sind bereits formatiert (Beträge, Datum; ADR-008). Erlaubt sind Texte ({@link String}), Listen und Maps
 * mit Text-Schlüsseln, beliebig verschachtelt. Ein fehlender Wert wird weggelassen, nicht als {@code null} geliefert.
 * Ob die Werte diese Regeln erfüllen, prüft die Bibliothek beim Übernehmen.
 *
 * <p>Der Datenkontext ist unveränderlich: Er kopiert Listen und Maps auf jeder Ebene und sperrt die Kopien. Spätere
 * Änderungen an den übergebenen Sammlungen wirken sich nicht aus. {@link #toString()} gibt keine Werte aus, weil sie
 * personenbezogen sein können.
 */
public record Datenkontext(Map<String, ?> werte) {

    public Datenkontext {
        werte = kopiereMap(Pflicht.wert(werte, "Die Werte des Datenkontexts"));
    }

    /** Nur die Zahl der Werte auf oberster Ebene, keine Schlüssel und keine Werte. */
    @Override
    public String toString() {
        return "Datenkontext[" + werte.size() + " Werte]";
    }

    private static <K> Map<K, @Nullable Object> kopiereMap(Map<K, ?> quelle) {
        Map<K, @Nullable Object> kopie = new LinkedHashMap<>();
        quelle.forEach((schluessel, wert) -> kopie.put(schluessel, kopiereWert(wert)));
        return Collections.unmodifiableMap(kopie);
    }

    private static @Nullable Object kopiereWert(@Nullable Object wert) {
        return switch (wert) {
            case Map<?, ?> map -> kopiereMap(map);
            case List<?> liste -> {
                List<@Nullable Object> kopie = new ArrayList<>(liste.size());
                liste.forEach(element -> kopie.add(kopiereWert(element)));
                yield Collections.unmodifiableList(kopie);
            }
            case null, default -> wert;
        };
    }
}
