package dieterbaier.letterwright.domain.model;

import dieterbaier.letterwright.validation.Validate;
import dieterbaier.letterwright.validation.ValidationException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * The values the host supplies for a letter template and a business reference, already formatted (ADR-008).
 *
 * <p>Only texts, lists and maps with text keys are allowed, nested to any depth, as the sandboxed placeholder language
 * requires. The data context is deeply immutable; the maps are sorted by key.
 *
 * <p>The {@link #checksum() checksum} depends only on the content, not on the order in which the host supplies the
 * keys of a map. The order of lists counts. Saving uses it to detect whether the data context has changed since the
 * preview (ADR-007).
 *
 * <p>Texts and keys must be valid Unicode. A lone surrogate could not be encoded unambiguously as UTF-8; two different
 * data contexts would otherwise get the same checksum.
 */
@ValueObject
public final class DataContext {

    private static final byte TEXT = 'T';
    private static final byte LIST = 'L';
    private static final byte MAP = 'M';

    private final Map<String, Object> values;
    private final Checksum checksum;

    private DataContext(Map<String, Object> values) {
        this.values = values;
        this.checksum = computeChecksum(values);
    }

    /** Takes a deep, immutable copy of the values. */
    public static DataContext from(Map<String, ?> values) {
        return new DataContext(copyMap(Validate.required(values, "The values of the data context"), ""));
    }

    public Map<String, Object> values() {
        return values;
    }

    /**
     * The values of both data contexts; for the same top-level key the value from {@code preferred} wins. This is how
     * the data for the proof PDF of a letter template is built from the sample data of its letterhead and its own
     * (ADR-022).
     */
    public DataContext extendedBy(DataContext preferred) {
        Map<String, Object> together = new TreeMap<>(values);
        together.putAll(Validate.required(preferred, "The preferred data context").values);
        return new DataContext(Collections.unmodifiableMap(together));
    }

    /** SHA-256 over an unambiguous, canonical encoding of the values. */
    public Checksum checksum() {
        return checksum;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DataContext context && values.equals(context.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        // Deliberately without values: they may be personal data and do not belong in logs.
        return "DataContext[checksum=" + checksum.hex() + "]";
    }

    private static Map<String, Object> copyMap(Map<?, ?> source, String path) {
        TreeMap<String, Object> copy = new TreeMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new ValidationException("Key at '" + path + "' is not text: " + entry.getKey());
            }
            String subpath = path.isEmpty() ? key : path + "." + key;
            checkUnicode(key, "Key " + subpath);
            copy.put(key, copyValue(entry.getValue(), subpath));
        }
        return Collections.unmodifiableMap(copy);
    }

    private static Object copyValue(Object value, String path) {
        return switch (value) {
            case String text -> checkUnicode(text, "Value at '" + path + "'");
            case Map<?, ?> map -> copyMap(map, path);
            case List<?> list -> {
                List<Object> copy = new ArrayList<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    copy.add(copyValue(list.get(i), path + "[" + i + "]"));
                }
                yield Collections.unmodifiableList(copy);
            }
            case null -> throw new ValidationException(
                    "Value at '" + path + "' is missing; a missing value is omitted, not supplied as null");
            default -> throw new ValidationException("Value at '" + path + "' is neither text, list nor map: "
                    + value.getClass().getName());
        };
    }

    private static String checkUnicode(String text, String label) {
        CharsetEncoder strict = StandardCharsets.UTF_8.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            strict.encode(CharBuffer.wrap(text));
        } catch (CharacterCodingException e) {
            throw new ValidationException(label + " is not valid Unicode (lone surrogate)", e);
        }
        return text;
    }

    private static Checksum computeChecksum(Map<String, Object> values) {
        MessageDigest sha256 = Checksum.sha256();
        encode(values, sha256);
        return new Checksum(HexFormat.of().formatHex(sha256.digest()));
    }

    // Every value starts with a type tag and its length. This makes the encoding unambiguous: no other data context
    // yields the same byte sequence, not even through texts that look like separators.
    private static void encode(Object value, MessageDigest target) {
        switch (value) {
            case String text -> {
                byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
                head(target, TEXT, bytes.length);
                target.update(bytes);
            }
            case List<?> list -> {
                head(target, LIST, list.size());
                list.forEach(element -> encode(element, target));
            }
            case Map<?, ?> map -> {
                // The maps are TreeMaps (see copyMap), so the order is sorted by key.
                head(target, MAP, map.size());
                map.forEach((key, element) -> {
                    encode(key, target);
                    encode(element, target);
                });
            }
            default -> throw new IllegalStateException("Not encodable: " + value.getClass().getName());
        }
    }

    private static void head(MessageDigest target, byte type, int length) {
        target.update(type);
        target.update(ByteBuffer.allocate(Integer.BYTES).putInt(length).array());
    }
}
