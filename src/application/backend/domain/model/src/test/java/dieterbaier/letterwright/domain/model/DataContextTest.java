package dieterbaier.letterwright.domain.model;

import static dieterbaier.letterwright.domain.model.Checks.rejects;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DataContextTest {

    private static final Map<String, Object> CUSTOMER = Map.of(
            "empfaenger", Map.of("name", "Erika Mustermann", "ort", "Köln"),
            "items", List.of("Beitrag 2026", "Mahngebühr"));

    @Test
    void sameContentYieldsSameChecksumRegardlessOfKeyOrder() {
        Map<String, Object> forward = new LinkedHashMap<>();
        forward.put("a", "1");
        forward.put("b", "2");
        Map<String, Object> backward = new LinkedHashMap<>();
        backward.put("b", "2");
        backward.put("a", "1");

        assertThat(DataContext.from(forward).checksum()).isEqualTo(DataContext.from(backward).checksum());
        assertThat(DataContext.from(forward)).isEqualTo(DataContext.from(backward))
                .hasSameHashCodeAs(DataContext.from(backward));
    }

    @Test
    void everyChangeOfAValueChangesTheChecksum() {
        Checksum before = DataContext.from(CUSTOMER).checksum();

        Map<String, Object> other = new HashMap<>(CUSTOMER);
        other.put("empfaenger", Map.of("name", "Erika Mustermann", "ort", "Bonn"));

        assertThat(DataContext.from(other).checksum()).isNotEqualTo(before);
    }

    @Test
    void theOrderInListsMatters() {
        DataContext from = DataContext.from(Map.of("items", List.of("a", "b")));
        DataContext ba = DataContext.from(Map.of("items", List.of("b", "a")));

        assertThat(from.checksum()).isNotEqualTo(ba.checksum());
        assertThat(from).isNotEqualTo(ba);
    }

    @Test
    void theEncodingIsUnambiguousEvenWhenTextsLookLikeStructure() {
        // Ohne Längen und Typkennungen ergäben diese Paare dieselbe Bytefolge.
        assertThat(DataContext.from(Map.of("a", "bc")).checksum())
                .isNotEqualTo(DataContext.from(Map.of("from", "c")).checksum());
        assertThat(DataContext.from(Map.of("x", List.of("from"))).checksum())
                .isNotEqualTo(DataContext.from(Map.of("x", List.of("a", "b"))).checksum());
        assertThat(DataContext.from(Map.of("x", "")).checksum())
                .isNotEqualTo(DataContext.from(Map.of("x", List.of())).checksum())
                .isNotEqualTo(DataContext.from(Map.of("x", Map.of())).checksum());
    }

    @Test
    void theChecksumIsStable() {
        // Festgeschrieben, damit eine Änderung der Kodierung auffällt: Snapshots, die vor der Änderung angelegt
        // wurden, würden sonst beim Speichern als geändert gelten. Der Wert ist unabhängig von dieser Implementierung
        // nachgerechnet (Python, hashlib): M,2 | T,"name" T,"Erika" | T,"posten" L,1 T,"a"; Längen als int32 big-endian.
        assertThat(DataContext.from(Map.of("name", "Erika", "items", List.of("a"))).checksum().hex())
                .isEqualTo("8b42f1fd375ad7eb974dddf7c451c706141bf7f5b3f84254b5ea0f1de7de7f69");
    }

    @Test
    void isDeeplyImmutableAndIndependentOfTheSource() {
        List<Object> items = new ArrayList<>(List.of("a"));
        Map<String, Object> source = new HashMap<>(Map.of("items", items, "unter", new HashMap<>(Map.of("k", "v"))));
        DataContext context = DataContext.from(source);

        items.add("b");
        source.put("generate", "x");

        assertThat(context.values()).containsOnlyKeys("items", "unter");
        assertThat(context.values().get("items")).isEqualTo(List.of("a"));
        assertThatThrownBy(() -> context.values().put("generate", "x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ((List<?>) context.values().get("items")).clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ((Map<?, ?>) context.values().get("unter")).clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void theValuesAreSortedByKey() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("b", "2");
        source.put("a", "1");

        assertThat(DataContext.from(source).values().keySet()).containsExactly("a", "b");
    }

    @Test
    void rejectsOtherTypesAndNamesThePath() {
        rejects(() -> DataContext.from(Map.of("betrag", Map.of("netto", 12))), "betrag.netto");
        rejects(() -> DataContext.from(Map.of("betrag", Map.of("netto", 12))), "java.lang.Integer");
        rejects(() -> DataContext.from(Map.of("items", List.of("a", List.of(1)))), "posten[1][0]");
    }

    @Test
    void rejectsMissingValues() {
        Map<String, Object> source = new HashMap<>();
        source.put("name", null);

        rejects(() -> DataContext.from(null), "Werte des Datenkontexts");
        rejects(() -> DataContext.from(source), "name");
        rejects(() -> DataContext.from(Map.of("items", Arrays.asList("a", null))), "posten[1]");
    }

    @Test
    void rejectsKeysThatAreNotText() {
        rejects(() -> DataContext.from(Map.of("unter", Map.of(1, "x"))), "unter");
    }

    @Test
    void rejectsLoneSurrogatesInValuesAndNamesThePath() {
        // Ohne diese Prüfung kodierte UTF-8 das Surrogat als '?', und beide Kontexte hätten dieselbe Prüfsumme.
        rejects(() -> DataContext.from(Map.of("x", "\uD800")), "x");
        rejects(() -> DataContext.from(Map.of("items", List.of("a\uDC00b"))), "posten[0]");
    }

    @Test
    void rejectsLoneSurrogatesInKeys() {
        rejects(() -> DataContext.from(Map.of("unter", Map.of("k\uD800", "x"))), "Schlüssel unter.k");
    }

    @Test
    void validSurrogatePairsAreAllowedAndDistinguishable() {
        DataContext emoji = DataContext.from(Map.of("gruss", "Hallo \uD83D\uDE00"));
        DataContext questionMark = DataContext.from(Map.of("gruss", "Hallo ?"));

        assertThat(emoji.values()).containsEntry("gruss", "Hallo 😀");
        assertThat(emoji.checksum()).isNotEqualTo(questionMark.checksum());
    }

    @Test
    void extendedByAnotherWhoseValuesTakePrecedence() {
        DataContext letterhead = DataContext.from(Map.of("absender", "Muster GmbH", "betreff", "Allgemein"));
        DataContext letterTemplate = DataContext.from(Map.of("betreff", "Mahnung", "betrag", "12,00 €"));

        DataContext together = letterhead.extendedBy(letterTemplate);

        assertThat(together).isEqualTo(DataContext.from(
                Map.of("absender", "Muster GmbH", "betreff", "Mahnung", "betrag", "12,00 €")));
        assertThat(together.values()).isUnmodifiable();
        assertThat(together.checksum()).isEqualTo(DataContext.from(together.values()).checksum());
        rejects(() -> letterhead.extendedBy(null), "vorrangige Datenkontext");
    }

    @Test
    void toStringShowsNoValues() {
        assertThat(DataContext.from(CUSTOMER).toString()).doesNotContain("Erika").contains("pruefsumme=");
    }

    @Test
    void isEqualOnlyWithDataContexts() {
        assertThat(DataContext.from(CUSTOMER)).isNotEqualTo(CUSTOMER);
    }
}
