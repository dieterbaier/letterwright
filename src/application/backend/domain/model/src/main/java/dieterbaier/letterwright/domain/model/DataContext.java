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
 * Die Werte, die der Host für ein Standardschreiben und einen fachlichen Bezug liefert, bereits formatiert (ADR-008).
 *
 * <p>Erlaubt sind nur Texte, Listen und Maps mit Text-Schlüsseln, beliebig verschachtelt. So verlangt es die
 * abgesicherte Platzhaltersprache. Der Datenkontext ist tief unveränderlich; die Maps sind nach Schlüssel sortiert.
 *
 * <p>Die {@link #checksum() Prüfsumme} hängt nur vom Inhalt ab, nicht von der Reihenfolge, in der der Host die
 * Schlüssel einer Map liefert. Die Reihenfolge von Listen zählt. Mit ihr erkennt das Speichern, ob sich der
 * Datenkontext seit der Vorschau geändert hat (ADR-007).
 *
 * <p>Texte und Schlüssel müssen gültiges Unicode sein. Ein einzelnes Surrogat ließe sich nicht eindeutig als UTF-8
 * kodieren; zwei verschiedene Datenkontexte bekämen sonst dieselbe Prüfsumme.
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

    /** Übernimmt eine tiefe, unveränderliche Kopie der Werte. */
    public static DataContext from(Map<String, ?> values) {
        return new DataContext(copyMap(Validate.required(values, "Die Werte des Datenkontexts"), ""));
    }

    public Map<String, Object> values() {
        return values;
    }

    /**
     * Die Werte beider Datenkontexte; bei gleichem Schlüssel auf oberster Ebene gilt der Wert aus {@code preferred}.
     * So entstehen die Daten für das Probe-PDF eines Standardschreibens aus den Beispieldaten seines Briefpapiers und
     * seinen eigenen (ADR-022).
     */
    public DataContext extendedBy(DataContext preferred) {
        Map<String, Object> together = new TreeMap<>(values);
        together.putAll(Validate.required(preferred, "Der vorrangige Datenkontext").values);
        return new DataContext(Collections.unmodifiableMap(together));
    }

    /** SHA-256 über eine eindeutige, kanonische Kodierung der Werte. */
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
        // Bewusst ohne Werte: Sie können personenbezogen sein und gehören nicht in Logs.
        return "Datenkontext[pruefsumme=" + checksum.hex() + "]";
    }

    private static Map<String, Object> copyMap(Map<?, ?> source, String path) {
        TreeMap<String, Object> copy = new TreeMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new ValidationException("Schlüssel unter '" + path + "' ist kein Text: " + entry.getKey());
            }
            String subpath = path.isEmpty() ? key : path + "." + key;
            checkUnicode(key, "Schlüssel " + subpath);
            copy.put(key, copyValue(entry.getValue(), subpath));
        }
        return Collections.unmodifiableMap(copy);
    }

    private static Object copyValue(Object value, String path) {
        return switch (value) {
            case String text -> checkUnicode(text, "Wert unter '" + path + "'");
            case Map<?, ?> map -> copyMap(map, path);
            case List<?> list -> {
                List<Object> copy = new ArrayList<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    copy.add(copyValue(list.get(i), path + "[" + i + "]"));
                }
                yield Collections.unmodifiableList(copy);
            }
            case null -> throw new ValidationException(
                    "Wert unter '" + path + "' fehlt; ein fehlender Wert wird weggelassen, nicht als null geliefert");
            default -> throw new ValidationException("Wert unter '" + path + "' ist weder Text, Liste noch Map: "
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
            throw new ValidationException(label + " ist kein gültiges Unicode (einzelnes Surrogat)", e);
        }
        return text;
    }

    private static Checksum computeChecksum(Map<String, Object> values) {
        MessageDigest sha256 = Checksum.sha256();
        encode(values, sha256);
        return new Checksum(HexFormat.of().formatHex(sha256.digest()));
    }

    // Jeder Wert beginnt mit einer Typkennung und seiner Länge. Damit ist die Kodierung eindeutig: Kein anderer
    // Datenkontext ergibt dieselbe Bytefolge, auch nicht durch Texte, die wie Trenner aussehen.
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
                // Die Maps sind TreeMaps (siehe kopiereMap), die Reihenfolge ist also nach Schlüssel sortiert.
                head(target, MAP, map.size());
                map.forEach((key, element) -> {
                    encode(key, target);
                    encode(element, target);
                });
            }
            default -> throw new IllegalStateException("Nicht kodierbar: " + value.getClass().getName());
        }
    }

    private static void head(MessageDigest target, byte type, int length) {
        target.update(type);
        target.update(ByteBuffer.allocate(Integer.BYTES).putInt(length).array());
    }
}
