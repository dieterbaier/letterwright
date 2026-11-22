package dieterbaier.standardschreiben.domaene.modell;

import dieterbaier.standardschreiben.validierung.Pruefe;
import dieterbaier.standardschreiben.validierung.Validierungsfehler;
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
 * <p>Die {@link #pruefsumme() Prüfsumme} hängt nur vom Inhalt ab, nicht von der Reihenfolge, in der der Host die
 * Schlüssel einer Map liefert. Die Reihenfolge von Listen zählt. Mit ihr erkennt das Speichern, ob sich der
 * Datenkontext seit der Vorschau geändert hat (ADR-007).
 *
 * <p>Texte und Schlüssel müssen gültiges Unicode sein. Ein einzelnes Surrogat ließe sich nicht eindeutig als UTF-8
 * kodieren; zwei verschiedene Datenkontexte bekämen sonst dieselbe Prüfsumme.
 */
@ValueObject
public final class Datenkontext {

    private static final byte TEXT = 'T';
    private static final byte LISTE = 'L';
    private static final byte MAP = 'M';

    private final Map<String, Object> werte;
    private final Pruefsumme pruefsumme;

    private Datenkontext(Map<String, Object> werte) {
        this.werte = werte;
        this.pruefsumme = berechnePruefsumme(werte);
    }

    /** Übernimmt eine tiefe, unveränderliche Kopie der Werte. */
    public static Datenkontext aus(Map<String, ?> werte) {
        return new Datenkontext(kopiereMap(Pruefe.pflicht(werte, "Die Werte des Datenkontexts"), ""));
    }

    public Map<String, Object> werte() {
        return werte;
    }

    /** SHA-256 über eine eindeutige, kanonische Kodierung der Werte. */
    public Pruefsumme pruefsumme() {
        return pruefsumme;
    }

    @Override
    public boolean equals(Object anderes) {
        return anderes instanceof Datenkontext kontext && werte.equals(kontext.werte);
    }

    @Override
    public int hashCode() {
        return werte.hashCode();
    }

    @Override
    public String toString() {
        // Bewusst ohne Werte: Sie können personenbezogen sein und gehören nicht in Logs.
        return "Datenkontext[pruefsumme=" + pruefsumme.hex() + "]";
    }

    private static Map<String, Object> kopiereMap(Map<?, ?> quelle, String pfad) {
        TreeMap<String, Object> kopie = new TreeMap<>();
        for (Map.Entry<?, ?> eintrag : quelle.entrySet()) {
            if (!(eintrag.getKey() instanceof String schluessel)) {
                throw new Validierungsfehler("Schlüssel unter '" + pfad + "' ist kein Text: " + eintrag.getKey());
            }
            String unterpfad = pfad.isEmpty() ? schluessel : pfad + "." + schluessel;
            pruefeUnicode(schluessel, "Schlüssel " + unterpfad);
            kopie.put(schluessel, kopiereWert(eintrag.getValue(), unterpfad));
        }
        return Collections.unmodifiableMap(kopie);
    }

    private static Object kopiereWert(Object wert, String pfad) {
        return switch (wert) {
            case String text -> pruefeUnicode(text, "Wert unter '" + pfad + "'");
            case Map<?, ?> map -> kopiereMap(map, pfad);
            case List<?> liste -> {
                List<Object> kopie = new ArrayList<>(liste.size());
                for (int i = 0; i < liste.size(); i++) {
                    kopie.add(kopiereWert(liste.get(i), pfad + "[" + i + "]"));
                }
                yield Collections.unmodifiableList(kopie);
            }
            case null -> throw new Validierungsfehler(
                    "Wert unter '" + pfad + "' fehlt; ein fehlender Wert wird weggelassen, nicht als null geliefert");
            default -> throw new Validierungsfehler("Wert unter '" + pfad + "' ist weder Text, Liste noch Map: "
                    + wert.getClass().getName());
        };
    }

    private static String pruefeUnicode(String text, String bezeichnung) {
        CharsetEncoder streng = StandardCharsets.UTF_8.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            streng.encode(CharBuffer.wrap(text));
        } catch (CharacterCodingException e) {
            throw new Validierungsfehler(bezeichnung + " ist kein gültiges Unicode (einzelnes Surrogat)", e);
        }
        return text;
    }

    private static Pruefsumme berechnePruefsumme(Map<String, Object> werte) {
        MessageDigest sha256 = Pruefsumme.sha256();
        kodiere(werte, sha256);
        return new Pruefsumme(HexFormat.of().formatHex(sha256.digest()));
    }

    // Jeder Wert beginnt mit einer Typkennung und seiner Länge. Damit ist die Kodierung eindeutig: Kein anderer
    // Datenkontext ergibt dieselbe Bytefolge, auch nicht durch Texte, die wie Trenner aussehen.
    private static void kodiere(Object wert, MessageDigest ziel) {
        switch (wert) {
            case String text -> {
                byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
                kopf(ziel, TEXT, bytes.length);
                ziel.update(bytes);
            }
            case List<?> liste -> {
                kopf(ziel, LISTE, liste.size());
                liste.forEach(element -> kodiere(element, ziel));
            }
            case Map<?, ?> map -> {
                // Die Maps sind TreeMaps (siehe kopiereMap), die Reihenfolge ist also nach Schlüssel sortiert.
                kopf(ziel, MAP, map.size());
                map.forEach((schluessel, element) -> {
                    kodiere(schluessel, ziel);
                    kodiere(element, ziel);
                });
            }
            default -> throw new IllegalStateException("Nicht kodierbar: " + wert.getClass().getName());
        }
    }

    private static void kopf(MessageDigest ziel, byte typ, int laenge) {
        ziel.update(typ);
        ziel.update(ByteBuffer.allocate(Integer.BYTES).putInt(laenge).array());
    }
}
