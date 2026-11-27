package dieterbaier.letterwright.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The values the host delivers for a letter template and a business reference.
 *
 * <p>The values are already formatted (amounts, dates; ADR-008). Allowed are texts ({@link String}), lists and maps
 * with text keys, nested to any depth. A missing value is left out, not delivered as {@code null}. The library checks
 * whether the values follow these rules when it takes them over.
 *
 * <p>The data context is immutable: it copies lists and maps on every level and makes the copies unmodifiable. Later
 * changes to the collections passed in have no effect. {@link #toString()} reveals no values because they may be
 * personal data.
 */
public record DataContext(Map<String, ?> values) {

    public DataContext {
        values = copyMap(Require.value(values, "The values of the data context"));
    }

    /** Only the number of top-level values, no keys and no values. */
    @Override
    public String toString() {
        return "DataContext[" + values.size() + " values]";
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
