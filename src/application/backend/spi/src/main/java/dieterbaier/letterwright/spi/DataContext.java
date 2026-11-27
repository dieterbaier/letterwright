package dieterbaier.letterwright.spi;

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
public record DataContext(Map<String, ?> values) {

    public DataContext {
        values = copyMap(Require.value(values, "Die Werte des Datenkontexts"));
    }

    /** Nur die Zahl der Werte auf oberster Ebene, keine Schlüssel und keine Werte. */
    @Override
    public String toString() {
        return "Datenkontext[" + values.size() + " Werte]";
    }

    private static <K> Map<K, @Nullable Object> copyMap(Map<K, ?> source) {
        Map<K, @Nullable Object> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, copyValue(value)));
        return Collections.unmodifiableMap(copy);
    }

    private static @Nullable Object copyValue(@Nullable Object value) {
        return switch (value) {
            case Map<?, ?> map -> copyMap(map);
            case List<?> list -> {
                List<@Nullable Object> copy = new ArrayList<>(list.size());
                list.forEach(element -> copy.add(copyValue(element)));
                yield Collections.unmodifiableList(copy);
            }
            case null, default -> value;
        };
    }
}
